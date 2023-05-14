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
    public void participate(Long recruitmentNo) {

        Recruitment recruitment = existsRecruitment(recruitmentNo, String.format("RecruitmentNo to Participate = [%d]", recruitmentNo));

        Long loginUserNo = SecurityUtil.getLoginUserNo();

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
    public void cancelParticipation(Long recruitmentNo) {

        existsRecruitment(recruitmentNo, String.format("RecruitmentNo to Cancel = [%d]", recruitmentNo));

        Long loginUserNo = SecurityUtil.getLoginUserNo();

        Participant findState = participantRepository.findByState(recruitmentNo, loginUserNo, State.JOIN_REQUEST)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_STATE,
                        String.format("UserNo = [%d], RecruitmentNo = [%d], State = [%s]",
                                loginUserNo, recruitmentNo, State.JOIN_REQUEST.name())));

        findState.joinCancel();
    }

    @Transactional
    @Override
    public void approvalParticipant(Long recruitmentNo, List<Long> userNo) {

        Recruitment recruitment = existsRecruitment(recruitmentNo, String.format("RecruitmentNo to Approval = [%d]", recruitmentNo));
        isRecruitmentOwner(recruitment);

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

        Recruitment recruitment = existsRecruitment(recruitmentNo, String.format("RecruitmentNo to Deport = [%d]", recruitmentNo));
        isRecruitmentOwner(recruitment);

        Participant findState = participantRepository.findByState(recruitmentNo, userNo, State.JOIN_APPROVAL)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_STATE,
                        String.format("UserNo = [%d], RecruitmentNo = [%d]", userNo, recruitmentNo)));

        findState.deport();
    }

    //모집글 존재 여부 판별 메서드
    private Recruitment existsRecruitment(Long recruitmentNo, String logErrorMessage){
        return recruitmentRepository.findPublishedByRecruitmentNo(recruitmentNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_RECRUITMENT,
                        logErrorMessage));
    }

    //모집글 방장 검증 메서드
    private void isRecruitmentOwner(Recruitment recruitment){
        Long loginUserNo = SecurityUtil.getLoginUserNo();

        if(!recruitment.getWriter().getUserNo().equals(loginUserNo)){
            throw new BusinessException(ErrorCode.FORBIDDEN_RECRUITMENT,
                    String.format("RecruitmentNo = [%d], UserNo = [%d]", recruitment.getRecruitmentNo(), loginUserNo));
        }
    }

}
