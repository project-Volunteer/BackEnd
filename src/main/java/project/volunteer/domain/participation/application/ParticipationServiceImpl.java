package project.volunteer.domain.participation.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.participation.dao.ParticipantRepository;
import project.volunteer.domain.participation.domain.Participant;
import project.volunteer.domain.recruitment.dao.RecruitmentRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.ParticipantState;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

import java.util.List;

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
