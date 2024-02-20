package project.volunteer.domain.sehedule.application;

import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.sehedule.application.dto.command.ScheduleUpsertCommand;
import project.volunteer.domain.sehedule.application.dto.command.RegularScheduleCreateCommand;

public interface ScheduleCommandUseCase {
    Long addSchedule(Recruitment recruitment, ScheduleUpsertCommand dto);

    void addRegularSchedule(Recruitment recruitment, RegularScheduleCreateCommand dto);

    Long editSchedule(Long scheduleNo, Recruitment recruitment, ScheduleUpsertCommand dto);

    void deleteSchedule(Long scheduleNo);

    void deleteAllSchedule(Long recruitmentNo);

}
