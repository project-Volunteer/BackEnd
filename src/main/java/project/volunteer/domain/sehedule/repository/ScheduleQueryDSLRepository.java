package project.volunteer.domain.sehedule.repository;

import java.time.LocalDate;
import java.util.List;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.sehedule.application.dto.query.ScheduleCalendarSearchResult;
import project.volunteer.domain.sehedule.application.dto.query.ScheduleDetailSearchResult;

public interface ScheduleQueryDSLRepository {
    List<ScheduleCalendarSearchResult> findScheduleDateBy(Recruitment recruitment, LocalDate toDate, LocalDate fromDate);

    ScheduleDetailSearchResult findScheduleDetailBy(Long scheduleNo);

    ScheduleDetailSearchResult findNearestScheduleDetailBy(Long recruitmentNo, LocalDate currentDate);

    Boolean existNearestSchedule(Long recruitmentNo, LocalDate currentDate);

}
