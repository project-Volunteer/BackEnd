package project.volunteer.domain.sehedule.repository;

import java.time.LocalDate;
import java.util.List;
import project.volunteer.domain.sehedule.application.dto.query.ScheduleCalendarSearchResult;
import project.volunteer.domain.sehedule.repository.dao.ScheduleDetail;

public interface ScheduleQueryDSLRepository {
    List<ScheduleCalendarSearchResult> findScheduleDateBy(Long recruitmentNo, LocalDate toDate, LocalDate fromDate);

    ScheduleDetail findScheduleDetailBy(Long scheduleNo);

    ScheduleDetail findNearestScheduleDetailBy(Long recruitmentNo, LocalDate currentDate);

    Boolean existNearestSchedule(Long recruitmentNo, LocalDate currentDate);

}
