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

    ScheduleDetailSearchResult searchClosestScheduleDetail(Long recruitmentNo, LocalDate currentDate);



    //활동 중인 스케줄 찾는 메서드(비관적 락 사용)
    Schedule findActivatedScheduleWithPERSSIMITIC_WRITE_Lock(Long scheduleNo);

    //활동 중인 스케줄 찾는 메서드
    Schedule findScheduleInProgress(Long scheduleNo);

    //출판된(삭제되지 않은) 스케줄 찾는 메서드
    Schedule findPublishedSchedule(Long scheduleNo);

}
