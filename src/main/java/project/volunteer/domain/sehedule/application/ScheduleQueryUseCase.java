package project.volunteer.domain.sehedule.application;

import java.time.LocalDate;
import java.util.List;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.sehedule.application.dto.query.ScheduleCalendarSearchResult;
import project.volunteer.domain.sehedule.application.dto.query.ScheduleDetailSearchResult;
import project.volunteer.domain.sehedule.domain.Schedule;

public interface ScheduleQueryUseCase {
    List<ScheduleCalendarSearchResult> searchScheduleCalender(Recruitment recruitment, LocalDate startDay, LocalDate endDay);

    ScheduleDetailSearchResult searchScheduleDetail(Long scheduleNo);

    ScheduleDetailSearchResult searchClosestScheduleDetail(Long recruitmentNo);

    // 삭제 되지 않고, 모집 중인 일정
    Schedule findScheduleInProgress(Long scheduleNo);

    // 삭제 되지 않은 일정
    Schedule findActivitedSchedule(Long scheduleNo);

    // 삭제 되지 않은 일정(비관적 락 사용)
    Schedule findScheduleInProgressWithPERSSIMITIC_WRITE_LOCK(Long scheduleNo);

}
