package project.volunteer.domain.recruitmentParticipation.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.recruitment.application.RecruitmentQueryService;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.user.application.UserService;
import project.volunteer.domain.user.domain.User;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecruitmentParticipationFacade {
    private final UserService userService;
    private final RecruitmentQueryService recruitmentQueryService;
    private final RecruitmentParticipationService participationService;

    @Transactional
    public Long joinRecruitmentTeam(Long userNo, Long recruitmentNo){
        User user = userService.findUser(userNo);
        Recruitment recruitment = recruitmentQueryService.findRecruitmentInProgress(recruitmentNo);
        return participationService.join(user, recruitment);
    }

    @Transactional
    public void cancelJoinRecruitmentTeam(Long userNo, Long recruitmentNo){
        User user = userService.findUser(userNo);
        Recruitment recruitment = recruitmentQueryService.findRecruitmentInProgress(recruitmentNo);
        participationService.cancelJoin(user, recruitment);
    }









    @Transactional
    public void approvalParticipantVolunteerTeam(List<Long> recruitmentParticipationNos, Long recruitmentNo){
        Recruitment recruitment = recruitmentQueryService.findActivatedRecruitment(recruitmentNo);

        participationService.approvalParticipant(recruitment, recruitmentParticipationNos);
    }

    @Transactional
    public void deportParticipantVolunteerTeam(Long recruitmentParticipationNo, Long recruitmentNo){
//        User user = userService.findUser(recruitmentParticipationNo);

        Recruitment recruitment = recruitmentQueryService.findActivatedRecruitment(recruitmentNo);

        participationService.deportParticipant(recruitment, recruitmentParticipationNo);
    }

}
