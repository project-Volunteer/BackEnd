package project.volunteer.domain.sehedule.application;

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

    @Override
    public List<ScheduleCalendarSearchResult> searchScheduleCalender(Recruitment recruitment, LocalDate startDay,
                                                                     LocalDate endDay) {
        return scheduleRepository.findScheduleDateBy(recruitment, startDay, endDay);
    }

    @Override
    public ScheduleDetailSearchResult searchScheduleDetail(Long scheduleNo) {
        validAndGetNotDeletedSchedule(scheduleNo);
        return scheduleRepository.findScheduleDetailBy(scheduleNo);
    }

    @Override
    public ScheduleDetailSearchResult searchClosestScheduleDetail(Long recruitmentNo, LocalDate currentDate) {
        if(!scheduleRepository.existNearestSchedule(recruitmentNo, currentDate)) {
            return null;
        }
        return scheduleRepository.findNearestScheduleDetailBy(recruitmentNo, currentDate);
    }







    @Override
    public Schedule findActivatedScheduleWithPERSSIMITIC_WRITE_Lock(Long scheduleNo) {
        Schedule findSchedule = validAndGetScheduleWithPERSSIMITIC_WRITE_Lock(scheduleNo);

        //일정 마감 일자 조회
        validateSchedulePeriod(findSchedule);
        return findSchedule;
    }

    @Override
    public Schedule findScheduleInProgress(Long scheduleNo) {
        Schedule schedule = validAndGetNotDeletedSchedule(scheduleNo);

        //일정 마감 일자 조회
        validateSchedulePeriod(schedule);
        return schedule;
    }

    @Override
    public Schedule findPublishedSchedule(Long scheduleNo) {
        return validAndGetNotDeletedSchedule(scheduleNo);
    }

    private Schedule validAndGetNotDeletedSchedule(Long scheduleNo) {
        return scheduleRepository.findNotDeletedSchedule(scheduleNo)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.NOT_EXIST_SCHEDULE, String.format("Schedule No = [%d]", scheduleNo)));
    }

    private Schedule validAndGetScheduleWithPERSSIMITIC_WRITE_Lock(Long scheduleNo) {
        return scheduleRepository.findValidScheduleWithPESSIMISTIC_WRITE_Lock(scheduleNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_SCHEDULE,
                        String.format("Schedule to participant = [%d]", scheduleNo)));
    }

    private void validateSchedulePeriod(Schedule schedule) {
        if (!schedule.isAvailableDate()) {
            throw new BusinessException(ErrorCode.EXPIRED_PERIOD_SCHEDULE,
                    String.format("ScheduleNo = [%d], participation period = [%s]", schedule.getScheduleNo(),
                            schedule.getScheduleTimeTable().getEndDay().toString()));
        }
    }
}
