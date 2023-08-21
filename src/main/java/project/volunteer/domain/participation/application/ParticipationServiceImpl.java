package project.volunteer.domain.participation.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.participation.application.dto.AllParticipantDetails;
import project.volunteer.domain.participation.application.dto.ParticipantDetails;
import project.volunteer.domain.participation.dao.ParticipantRepository;
import project.volunteer.domain.participation.dao.dto.ParticipantStateDetails;
import project.volunteer.domain.participation.domain.Participant;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.ParticipantState;
import project.volunteer.global.common.dto.StateResponse;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipationServiceImpl implements ParticipationService{
    private final ParticipantRepository participantRepository;

    @Transactional
    @Override
    public void participate(User user, Recruitment recruitment) {
        //참여 가능 인원이 꽉 찬경우
        if(recruitment.isFullTeamMember()){
            throw  new BusinessException(ErrorCode.INSUFFICIENT_CAPACITY,
                    String.format("RecruitmentNo = [%d], Recruiting participant num = [%d]", recruitment.getRecruitmentNo(), recruitment.getVolunteerNum()));
        }

        //재신청 or 신규 신청
        participantRepository.findByRecruitmentAndParticipant(recruitment, user)
                .ifPresentOrElse(
                        p -> {
                            //중복 신청인 경우
                            if(p.isEqualState(ParticipantState.JOIN_REQUEST) || p.isEqualState(ParticipantState.JOIN_APPROVAL)){
                                throw new BusinessException(ErrorCode.DUPLICATE_PARTICIPATION,
                                        String.format("UserNo = [%d], RecruitmentNo = [%d], State = [%s]",
                                                user.getUserNo(), recruitment.getRecruitmentNo(), p.getState().name()));
                            }

                            //재신청에 해당(재신청 가능 상태 리스트: 팀 신청 취소, 팀 탈퇴, 팀 강제탈퇴)
                            p.updateState(ParticipantState.JOIN_REQUEST);
                        },
                        () -> {
                            //첫 신청인 경우
                            Participant newParticipant = Participant.createParticipant(recruitment, user, ParticipantState.JOIN_REQUEST);
                            participantRepository.save(newParticipant);
                        }
                );
    }

    @Transactional
    @Override
    public void cancelParticipation(User user, Recruitment recruitment) {
        Participant findState = participantRepository.findByRecruitmentAndParticipantAndState(recruitment, user, ParticipantState.JOIN_REQUEST)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_STATE,
                        String.format("UserNo = [%d], RecruitmentNo = [%d], State = [%s]",
                                user.getUserNo(), recruitment.getRecruitmentNo(), ParticipantState.JOIN_REQUEST.name())));

        findState.updateState(ParticipantState.JOIN_CANCEL);
    }

    @Transactional
    @Override
    public void approvalParticipant(Recruitment recruitment, List<Long> userNo) {
        //팀원 승인 가능 인원 검증
        Integer remainNum = recruitment.getAvailableTeamMemberCount();
        if(remainNum < userNo.size()){
            throw new BusinessException(ErrorCode.INSUFFICIENT_APPROVAL_CAPACITY, new Integer[]{remainNum},
                    String.format("RecruitmentNo = [%d], Available participant num = [%d], " +
                            "Approval participants num = [%d]", recruitment.getRecruitmentNo(), remainNum, userNo.size()));
        }

        List<Participant> findParticipants = participantRepository.findByRecruitment_RecruitmentNoAndParticipant_UserNoIn(recruitment.getRecruitmentNo(), userNo);
        for(Participant p : findParticipants){
            if(!p.isEqualState(ParticipantState.JOIN_REQUEST)){
                throw new BusinessException(ErrorCode.INVALID_STATE,
                        String.format("UserNo = [%d], RecruitmentNo = [%d], State = [%s]",
                                p.getParticipant().getUserNo(), recruitment.getRecruitmentNo(), p.getState().name()));
            }
            p.updateState(ParticipantState.JOIN_APPROVAL);

            //봉사 모집글 팀원인원 증가
            recruitment.increaseTeamMember();
        }
    }

    @Transactional
    @Override
    public void deportParticipant(Recruitment recruitment, User user) {
        Participant findState = participantRepository.findByRecruitmentAndParticipantAndState(recruitment, user, ParticipantState.JOIN_APPROVAL)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_STATE,
                        String.format("UserNo = [%d], RecruitmentNo = [%d]", user.getUserNo(), recruitment.getRecruitmentNo())));

        findState.updateState(ParticipantState.DEPORT);

        //봉사 모집글 팀원인원 감소
        recruitment.decreaseTeamMember();
    }

    @Override
    public AllParticipantDetails findAllParticipantDto(Long recruitmentNo) {
        List<ParticipantDetails> approvedList = new ArrayList<>();
        List<ParticipantDetails> requiredList = new ArrayList<>();

        //최적화한 쿼리(쿼리 1번)
        List<ParticipantStateDetails> participants = participantRepository.findParticipantsByOptimization(recruitmentNo,
                List.of(ParticipantState.JOIN_REQUEST, ParticipantState.JOIN_APPROVAL));

        participants.stream()
                .forEach(p -> {
                    if(p.getState().equals(ParticipantState.JOIN_REQUEST)){
                        requiredList.add(new ParticipantDetails(p.getUserNo(), p.getNickName(), p.getImageUrl()));
                    }else{
                        approvedList.add(new ParticipantDetails(p.getUserNo(), p.getNickName(), p.getImageUrl()));
                    }
                });

        return new AllParticipantDetails(approvedList, requiredList);
    }

    /**
     * L1 : 봉사 모집 기간 마감
     * L2 : 팀 신청, 팀 신청 승인
     * L3 : 팀 신청 인원 마감
     * L4 : 팀 신청 가능(팀 신청 취소, 팀 탈퇴, 팀 강제 탈퇴)
     */
    @Override
    public String findParticipationState(Recruitment recruitment, User user) {
        Optional<Participant> findParticipant = participantRepository.findByRecruitmentAndParticipant(recruitment, user);

        //봉사 모집 기간 만료
        if(recruitment.isDoneDate()){
            return StateResponse.DONE.getId();
        }

        //팀 신청
        if(findParticipant.isPresent() && findParticipant.get().isEqualState(ParticipantState.JOIN_REQUEST)){
            return StateResponse.PENDING.getId();
        }

        //팀 신청 승인
        if(findParticipant.isPresent() && findParticipant.get().isEqualState(ParticipantState.JOIN_APPROVAL)){
            return StateResponse.APPROVED.getId();
        }

        //팀 신청 인원 마감
        if(recruitment.isFullTeamMember()){
            return StateResponse.FULL.getId();
        }

        //팀 신청 가능(팀 신청 취소, 팀 탈퇴, 팀 강제 탈퇴, 신규 팀 신청)
        return StateResponse.AVAILABLE.getId();
    }

    @Override
    public Participant findParticipation(Long recruitmentNo, Long userNo) {
        return participantRepository.findByRecruitment_RecruitmentNoAndParticipant_UserNo(recruitmentNo, userNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_PARTICIPATION,
                        String.format("RecruitmentNo = [%d], UserNo = [%d]", recruitmentNo, userNo)));
    }

    @Override
    @Transactional
    public void deleteParticipations(Long recruitmentNo) {
        //연관관계 끊기
        participantRepository.findByRecruitment_RecruitmentNo(recruitmentNo)
                .forEach(p -> {
                    p.delete();
                    p.removeUserAndRecruitment();
                });
    }

}
