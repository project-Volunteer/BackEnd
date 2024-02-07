package project.volunteer.domain.participation.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.participation.application.ParticipationService;
import project.volunteer.domain.recruitment.application.RecruitmentCommandUseCase;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.user.application.UserService;
import project.volunteer.domain.user.domain.User;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipationFacade {
    private final UserService userService;
    private final RecruitmentCommandUseCase recruitmentService;
    private final ParticipationService participationService;

    @Transactional
    public void participateVolunteerTeam(Long userNo, Long recruitmentNo){
        User user = userService.findUser(userNo);

        Recruitment recruitment = recruitmentService.findActivatedRecruitment(recruitmentNo);

        participationService.participate(user, recruitment);
    }

    @Transactional
    public void cancelParticipationVolunteerTeam(Long userNo, Long recruitmentNo){
        User user = userService.findUser(userNo);

        Recruitment recruitment = recruitmentService.findActivatedRecruitment(recruitmentNo);

        participationService.cancelParticipation(user, recruitment);
    }

    @Transactional
    public void approvalParticipantVolunteerTeam(List<Long> userNos, Long recruitmentNo){
        Recruitment recruitment = recruitmentService.findActivatedRecruitment(recruitmentNo);

        participationService.approvalParticipant(recruitment, userNos);
    }

    @Transactional
    public void deportParticipantVolunteerTeam(Long userNo, Long recruitmentNo){
        User user = userService.findUser(userNo);

        Recruitment recruitment = recruitmentService.findActivatedRecruitment(recruitmentNo);

        participationService.deportParticipant(recruitment, user);
    }

}
