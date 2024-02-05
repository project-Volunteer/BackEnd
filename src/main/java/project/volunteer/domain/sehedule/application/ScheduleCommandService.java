package project.volunteer.domain.sehedule.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.sehedule.application.dto.command.RegularScheduleCreateCommand;
import project.volunteer.domain.sehedule.application.timetableCreator.RepeatTimetableCreateProvider;
import project.volunteer.domain.sehedule.application.timetableCreator.RepeatTimetableCreator;
import project.volunteer.domain.sehedule.repository.ScheduleJdbcRepository;
import project.volunteer.domain.sehedule.repository.ScheduleRepository;
import project.volunteer.domain.sehedule.domain.Schedule;
import project.volunteer.domain.sehedule.application.dto.command.ScheduleUpsertCommand;
import project.volunteer.global.common.component.IsDeleted;
import project.volunteer.global.common.component.Timetable;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

import java.util.*;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ScheduleCommandService implements ScheduleCommandUseCase {
    private final ScheduleRepository scheduleRepository;
    private final ScheduleJdbcRepository scheduleJdbcRepository;
    private final RepeatTimetableCreateProvider repeatTimetableCreateProvider;

    @Override
    public Long addSchedule(final Recruitment recruitment, final ScheduleUpsertCommand command) {
        final Schedule schedule = command.toDomain(recruitment);
        return scheduleRepository.save(schedule).getScheduleNo();
    }

    @Override
    public void addRegularSchedule(final Recruitment recruitment, final RegularScheduleCreateCommand command) {
        RepeatTimetableCreator timetableCreator = repeatTimetableCreateProvider.getCreator(
                command.getRepeatPeriod());
        final List<Timetable> scheduleTimetable = timetableCreator.create(command.getRecruitmentTimetable(),
                command.getRepeatPeriod());
        final List<Schedule> schedules = command.toDomains(scheduleTimetable, recruitment);

        scheduleJdbcRepository.saveAll(schedules);
    }

    @Override
    public Long editSchedule(final Long scheduleNo, final Recruitment recruitment,
                             final ScheduleUpsertCommand command) {
        final Schedule findSchedule = validAndGetSchedule(scheduleNo);
        findSchedule.change(recruitment, command.getTimetable(), command.getContent(), command.getOrganizationName(),
                command.getAddress(), command.getMaxParticipationNum());

        return findSchedule.getScheduleNo();
    }

    @Override
    public void deleteSchedule(final Long scheduleNo) {
        Schedule findSchedule = validAndGetSchedule(scheduleNo);
        findSchedule.delete();
    }

    @Override
    public void deleteAllSchedule(Long recruitmentNo) {
        scheduleRepository.bulkUpdateIsDeleted(IsDeleted.Y, recruitmentNo);
    }

    //일정 유효성 검사
    private Schedule validAndGetSchedule(Long scheduleNo) {
        return scheduleRepository.findNotDeletedSchedule(scheduleNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_SCHEDULE,
                        String.format("Schedule No = [%d]", scheduleNo)));
    }

}
