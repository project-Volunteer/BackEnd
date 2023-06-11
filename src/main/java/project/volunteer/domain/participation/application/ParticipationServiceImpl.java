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
import project.volunteer.global.common.component.State;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;
import project.volunteer.global.util.SecurityUtil;

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

        Recruitment recruitment = isActivatediRecruitment(recruitmentNo, String.format("RecruitmentNo to Participate = [%d]", recruitmentNo));

        //참여 가능 인원이 꽉 찬경우
        if(participantRepository.countAvailableParticipants(recruitmentNo) ==recruitment.getVolunteerNum()){
            throw  new BusinessException(ErrorCode.INSUFFICIENT_CAPACITY,
                    String.format("RecruitmentNo = [%d], Recruiting participant num = [%d]", recruitmentNo, recruitment.getVolunteerNum()));
        }

        participantRepository.findByRecruitment_RecruitmentNoAndParticipant_UserNo(recruitmentNo, loginUserNo)
                .ifPresentOrElse(
                        p -> {
                            //중복 신청인 경우
                            if (p.getState().equals(State.JOIN_REQUEST) || p.getState().equals(State.JOIN_APPROVAL)){
                                throw new BusinessException(ErrorCode.DUPLICATE_PARTICIPATION,
                                        String.format("UserNo = [%d], RecruitmentNo = [%d], State = [%s]",
                                                loginUserNo, recruitmentNo, p.getState().name()));
                            }
                            //재신청에 해당(재신청 가능 상태 리스트: 팀 신청 취소, 팀 탈퇴, 팀 강제탈퇴)
                            p.joinRequest();
                        },
                        () -> {
                            //인증을 거쳤으므로 get() 으로 가져와도 무방
                            User participant = userRepository.findById(loginUserNo).get();
                            //첫 신청인 경우
                            Participant newParticipant = Participant.builder()
                                    .participant(participant)
                                    .recruitment(recruitment)
                                    .state(State.JOIN_REQUEST)
                                    .build();
                            participantRepository.save(newParticipant);
                        }
                );
    }

    @Transactional
    @Override
    public void cancelParticipation(Long loginUserNo, Long recruitmentNo) {

        isActivatediRecruitment(recruitmentNo, String.format("RecruitmentNo to Cancel = [%d]", recruitmentNo));

        Participant findState = participantRepository.findByRecruitmentNoAndParticipantNoAndState(recruitmentNo, loginUserNo, State.JOIN_REQUEST)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_STATE,
                        String.format("UserNo = [%d], RecruitmentNo = [%d], State = [%s]",
                                loginUserNo, recruitmentNo, State.JOIN_REQUEST.name())));

        findState.joinCancel();
    }

    @Transactional
    @Override
    public void approvalParticipant(Long recruitmentNo, List<Long> userNo) {

        Recruitment recruitment = isActivatediRecruitment(recruitmentNo, String.format("RecruitmentNo to Approval = [%d]", recruitmentNo));

        //승인가능인원수 초과
        Integer remainNum = recruitment.getVolunteerNum() - participantRepository.countAvailableParticipants(recruitmentNo);
        if(remainNum < userNo.size()){
            throw new BusinessException(ErrorCode.INSUFFICIENT_APPROVAL_CAPACITY, new Integer[]{remainNum},
                    String.format("RecruitmentNo = [%d], Available participant num = [%d], " +
                            "Approval participants num", recruitmentNo, remainNum, userNo.size()));
        }

        List<Participant> findParticipants = participantRepository.findByRecruitment_RecruitmentNoAndParticipant_UserNoIn(recruitmentNo, userNo);
        findParticipants.stream()
                .forEach(p -> {
                    if(!p.getState().equals(State.JOIN_REQUEST)){
                        throw new BusinessException(ErrorCode.INVALID_STATE,
                                String.format("UserNo = [%d], RecruitmentNo = [%d], State = [%s]",
                                        p.getParticipant().getUserNo(), recruitmentNo, p.getState().name()));
                    }
                    p.joinApprove();
                });
    }

    @Transactional
    @Override
    public void deportParticipant(Long recruitmentNo, Long userNo) {

        Participant findState = participantRepository.findByRecruitmentNoAndParticipantNoAndState(recruitmentNo, userNo, State.JOIN_APPROVAL)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_STATE,
                        String.format("UserNo = [%d], RecruitmentNo = [%d]", userNo, recruitmentNo)));

        findState.deport();
    }

    //신청 가능한 모집글인지 판별
    private Recruitment isActivatediRecruitment(Long recruitmentNo, String logErrorMessage){
        return recruitmentRepository.findActivatedRecruitment(recruitmentNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_RECRUITMENT,
                        logErrorMessage));
    }

}
