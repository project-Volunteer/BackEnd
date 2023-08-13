package project.volunteer.domain.participation.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.image.domain.Image;
import project.volunteer.domain.participation.application.dto.AllParticipantDetails;
import project.volunteer.domain.participation.application.dto.ParticipantDetails;
import project.volunteer.domain.participation.dao.ParticipantRepository;
import project.volunteer.domain.participation.dao.dto.ParticipantStateDetails;
import project.volunteer.domain.participation.domain.Participant;
import project.volunteer.domain.recruitment.application.dto.RecruitmentDetails;
import project.volunteer.domain.recruitment.dao.RecruitmentRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.ParticipantState;
import project.volunteer.global.common.component.RealWorkCode;
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

    private final UserRepository userRepository;
    private final RecruitmentRepository recruitmentRepository;
    private final ParticipantRepository participantRepository;

    @Transactional
    @Override
    public void participate(Long loginUserNo, Long recruitmentNo) {

        Recruitment recruitment = isActivatediRecruitment(recruitmentNo);

        //참여 가능 인원이 꽉 찬경우
        if(recruitment.isFullTeamMember()){
            throw  new BusinessException(ErrorCode.INSUFFICIENT_CAPACITY,
                    String.format("RecruitmentNo = [%d], Recruiting participant num = [%d]", recruitmentNo, recruitment.getVolunteerNum()));
        }

        //재신청 or 신규 신청
        participantRepository.findByRecruitment_RecruitmentNoAndParticipant_UserNo(recruitmentNo, loginUserNo)
                .ifPresentOrElse(
                        p -> {
                            //중복 신청인 경우
                            if(p.isEqualState(ParticipantState.JOIN_REQUEST) || p.isEqualState(ParticipantState.JOIN_APPROVAL)){
                                throw new BusinessException(ErrorCode.DUPLICATE_PARTICIPATION,
                                        String.format("UserNo = [%d], RecruitmentNo = [%d], State = [%s]",
                                                loginUserNo, recruitmentNo, p.getState().name()));
                            }

                            //재신청에 해당(재신청 가능 상태 리스트: 팀 신청 취소, 팀 탈퇴, 팀 강제탈퇴)
                            p.updateState(ParticipantState.JOIN_REQUEST);
                        },
                        () -> {
                            //인증을 거쳤으므로 get() 으로 가져와도 무방
                            User participant = userRepository.findById(loginUserNo).get();

                            //첫 신청인 경우
                            Participant newParticipant = Participant.createParticipant(recruitment, participant, ParticipantState.JOIN_REQUEST);
                            participantRepository.save(newParticipant);
                        }
                );
    }

    @Transactional
    @Override
    public void cancelParticipation(Long loginUserNo, Long recruitmentNo) {

        isActivatediRecruitment(recruitmentNo);

        Participant findState = participantRepository.findByRecruitmentNoAndParticipantNoAndState(recruitmentNo, loginUserNo, ParticipantState.JOIN_REQUEST)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_STATE,
                        String.format("UserNo = [%d], RecruitmentNo = [%d], State = [%s]",
                                loginUserNo, recruitmentNo, ParticipantState.JOIN_REQUEST.name())));

        findState.updateState(ParticipantState.JOIN_CANCEL);
    }

    @Transactional
    @Override
    public void approvalParticipant(Long recruitmentNo, List<Long> userNo) {

        Recruitment recruitment = isActivatediRecruitment(recruitmentNo);

        //팀원 승인 가능 인원 검증
        Integer remainNum = recruitment.getAvailableTeamMemberCount();
        if(remainNum < userNo.size()){
            throw new BusinessException(ErrorCode.INSUFFICIENT_APPROVAL_CAPACITY, new Integer[]{remainNum},
                    String.format("RecruitmentNo = [%d], Available participant num = [%d], " +
                            "Approval participants num = [%d]", recruitmentNo, remainNum, userNo.size()));
        }

        List<Participant> findParticipants = participantRepository.findByRecruitment_RecruitmentNoAndParticipant_UserNoIn(recruitmentNo, userNo);
        for(Participant p : findParticipants){
            if(!p.isEqualState(ParticipantState.JOIN_REQUEST)){
                throw new BusinessException(ErrorCode.INVALID_STATE,
                        String.format("UserNo = [%d], RecruitmentNo = [%d], State = [%s]",
                                p.getParticipant().getUserNo(), recruitmentNo, p.getState().name()));
            }
            p.updateState(ParticipantState.JOIN_APPROVAL);

            //봉사 모집글 팀원인원 증가
            recruitment.increaseTeamMember();
        }
    }

    @Transactional
    @Override
    public void deportParticipant(Long recruitmentNo, Long userNo) {

        Recruitment recruitment = isActivatediRecruitment(recruitmentNo);

        Participant findState = participantRepository.findByRecruitmentNoAndParticipantNoAndState(recruitmentNo, userNo, ParticipantState.JOIN_APPROVAL)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_STATE,
                        String.format("UserNo = [%d], RecruitmentNo = [%d]", userNo, recruitmentNo)));

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
    public String findParticipationState(Recruitment recruitment, Long loginUserNo) {
        Optional<Participant> findParticipant = participantRepository.findByRecruitment_RecruitmentNoAndParticipant_UserNo(
                recruitment.getRecruitmentNo(), loginUserNo);

        //봉사 모집 기간 만료
        if(!recruitment.isAvailableDate()){
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


    //신청 가능한 모집글인지 판별
    private Recruitment isActivatediRecruitment(Long recruitmentNo){
        //봉사 모집글 조회(삭제되지 않은지)
        Recruitment findRecruitment = recruitmentRepository.findValidRecruitment(recruitmentNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_RECRUITMENT,
                        String.format("RecruitmentNo = [%d]", recruitmentNo)));

        //봉사 모집글 마감 일자 조회
        if(!findRecruitment.isAvailableDate()){
            throw new BusinessException(ErrorCode.EXPIRED_PERIOD_RECRUITMENT,
                    String.format("RecruitmentNo = [%d]", recruitmentNo));
        }
        return findRecruitment;
    }
}
