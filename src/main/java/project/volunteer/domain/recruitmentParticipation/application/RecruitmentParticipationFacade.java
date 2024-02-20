package project.volunteer.domain.recruitmentParticipation.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.recruitment.application.RecruitmentQueryUseCase;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.user.application.UserService;
import project.volunteer.domain.user.domain.User;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecruitmentParticipationFacade {
    private final UserService userService;
    private final RecruitmentQueryUseCase recruitmentQueryUseCase;
    private final RecruitmentParticipationService participationService;

    @Transactional
    public Long joinRecruitmentTeam(Long userNo, Long recruitmentNo){
        User user = userService.findUser(userNo);
        Recruitment recruitment = recruitmentQueryUseCase.findRecruitmentInProgress(recruitmentNo);
        return participationService.join(user, recruitment);
    }

    @Transactional
    public void cancelJoinRecruitmentTeam(Long userNo, Long recruitmentNo){
        User user = userService.findUser(userNo);
        Recruitment recruitment = recruitmentQueryUseCase.findRecruitmentInProgress(recruitmentNo);
        participationService.cancelJoin(user, recruitment);
    }

    @Transactional
    public void approveJoinRecruitmentTeam(List<Long> recruitmentParticipationNos, Long recruitmentNo){
        Recruitment recruitment = recruitmentQueryUseCase.findActivatedRecruitment(recruitmentNo);
        participationService.approveJoin(recruitment, recruitmentParticipationNos);
    }

    @Transactional
    public void deportRecruitmentTeam(List<Long> recruitmentParticipationNos, Long recruitmentNo){
        Recruitment recruitment = recruitmentQueryUseCase.findActivatedRecruitment(recruitmentNo);
        participationService.deport(recruitment, recruitmentParticipationNos);
    }

}
