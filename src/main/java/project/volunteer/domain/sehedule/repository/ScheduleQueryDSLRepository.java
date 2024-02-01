package project.volunteer.domain.sehedule.repository;

import java.time.LocalDate;
import java.util.List;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.sehedule.application.dto.query.ScheduleCalendarSearchResult;

public interface ScheduleQueryDSLRepository {
    List<ScheduleCalendarSearchResult> findScheduleDate(Recruitment recruitment, LocalDate toDate, LocalDate fromDate);

}
