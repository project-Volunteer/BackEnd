package project.volunteer.domain.sehedule.application;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.sehedule.application.dto.query.ScheduleCalendarSearchResult;
import project.volunteer.domain.sehedule.application.dto.query.ScheduleDetailSearchResult;
import project.volunteer.domain.sehedule.repository.ScheduleRepository;
import project.volunteer.domain.sehedule.domain.Schedule;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ScheduleQueryService implements ScheduleQueryUseCase {
    private final ScheduleRepository scheduleRepository;
    private final Clock clock;

    @Override
    public List<ScheduleCalendarSearchResult> searchScheduleCalender(final Recruitment recruitment,
                                                                     final LocalDate startDay,
                                                                     final LocalDate endDay) {
        return scheduleRepository.findScheduleDateBy(recruitment, startDay, endDay);
    }

    @Override
    public ScheduleDetailSearchResult searchScheduleDetail(final Long scheduleNo) {
        validAndGetNotDeletedSchedule(scheduleNo);
        return scheduleRepository.findScheduleDetailBy(scheduleNo);
    }

    @Override
    public ScheduleDetailSearchResult searchClosestScheduleDetail(final Long recruitmentNo) {
        final LocalDate now = LocalDate.now(clock);
        if (!scheduleRepository.existNearestSchedule(recruitmentNo, now)) {
            return null;
        }
        return scheduleRepository.findNearestScheduleDetailBy(recruitmentNo, now);
    }

    @Override
    public Schedule findScheduleInProgress(final Long scheduleNo) {
        final Schedule schedule = validAndGetNotDeletedSchedule(scheduleNo);
        schedule.checkDoneDate(LocalDate.now(clock));
        return schedule;
    }

    @Override
    public Schedule findActivitedSchedule(final Long scheduleNo) {
        return validAndGetNotDeletedSchedule(scheduleNo);
    }

    private Schedule validAndGetNotDeletedSchedule(final Long scheduleNo) {
        return scheduleRepository.findNotDeletedSchedule(scheduleNo)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.NOT_EXIST_SCHEDULE, String.format("ScheduleNo = [%d]", scheduleNo)));
    }

    @Override
    public Schedule findScheduleInProgressWithPERSSIMITIC_WRITE_LOCK(final Long scheduleNo) {
        final Schedule schedule = scheduleRepository.findNotDeletedScheduleByPERSSIMITIC_LOCK(scheduleNo)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.NOT_EXIST_SCHEDULE, String.format("ScheduleNo = [%d]", scheduleNo)));

        schedule.checkDoneDate(LocalDate.now(clock));
        return schedule;
    }

}
