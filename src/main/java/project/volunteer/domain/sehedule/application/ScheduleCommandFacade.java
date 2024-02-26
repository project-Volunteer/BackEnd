package project.volunteer.domain.sehedule.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.recruitment.application.RecruitmentQueryUseCase;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.scheduleParticipation.service.ScheduleParticipationCommandUseCase;
import project.volunteer.domain.sehedule.application.dto.command.ScheduleUpsertCommand;

@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleCommandFacade {
    private final RecruitmentQueryUseCase recruitmentQueryUseCase;
    private final ScheduleCommandUseCase scheduleCommandService;
    private final ScheduleParticipationCommandUseCase scheduleParticipationService;

    public Long registerSchedule(Long recruitmentNo, ScheduleUpsertCommand command) {
        Recruitment recruitment = recruitmentQueryUseCase.findActivatedRecruitment(recruitmentNo);

        return scheduleCommandService.addSchedule(recruitment, command);
    }

    public Long updateSchedule(Long recruitmentNo, Long scheduleNo, ScheduleUpsertCommand command) {
        Recruitment recruitment = recruitmentQueryUseCase.findActivatedRecruitment(recruitmentNo);

        return scheduleCommandService.editSchedule(scheduleNo, recruitment, command);
    }

    public void deleteSchedule(Long scheduleNo) {
        scheduleCommandService.deleteSchedule(scheduleNo);

        scheduleParticipationService.deleteAllScheduleParticipationBySchedule(scheduleNo);
    }

}
