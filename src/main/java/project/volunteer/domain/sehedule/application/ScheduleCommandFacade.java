package project.volunteer.domain.sehedule.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.recruitment.application.RecruitmentService;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.scheduleParticipation.service.ScheduleParticipationService;
import project.volunteer.domain.sehedule.application.dto.command.ScheduleUpsertCommand;

@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleCommandFacade {
    private final RecruitmentService recruitmentService;
    private final ScheduleCommandUseCase scheduleCommandService;
    private final ScheduleParticipationService scheduleParticipationService;

    public Long registerSchedule(Long recruitmentNo, ScheduleUpsertCommand command) {
        Recruitment recruitment = recruitmentService.findPublishedRecruitment(recruitmentNo);

        return scheduleCommandService.addSchedule(recruitment, command);
    }

    public Long updateSchedule(Long recruitmentNo, Long scheduleNo, ScheduleUpsertCommand command) {
        Recruitment recruitment = recruitmentService.findPublishedRecruitment(recruitmentNo);

        return scheduleCommandService.editSchedule(scheduleNo, recruitment, command);
    }

    public void deleteSchedule(Long recruitmentNo, Long scheduleNo) {
        recruitmentService.findPublishedRecruitment(recruitmentNo);

        scheduleCommandService.deleteSchedule(scheduleNo);

        scheduleParticipationService.deleteScheduleParticipation(scheduleNo);
    }

}
