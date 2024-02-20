package project.volunteer.domain.sehedule.repository;

import java.util.List;
import project.volunteer.domain.sehedule.domain.Schedule;

public interface ScheduleJdbcRepository {
    void saveAll(List<Schedule> schedules);

}
