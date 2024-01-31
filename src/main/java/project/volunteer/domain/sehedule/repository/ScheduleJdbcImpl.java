package project.volunteer.domain.sehedule.repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import project.volunteer.domain.sehedule.domain.Schedule;

@Repository
@RequiredArgsConstructor
public class ScheduleJdbcImpl implements ScheduleJdbcRepository{
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void saveAll(List<Schedule> schedules) {
        final String sql = "INSERT INTO vlt_schedule "
                + "(start_day, end_day, hour_format, start_time, progress_time, organization_name, sido, sigungu, details, full_name, "
                + "content, volunteer_num, current_volunteer_num, is_deleted, recruitmentno, created_date, modified_date) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Schedule schedule = schedules.get(i);
                ps.setDate(1, Date.valueOf(schedule.getScheduleTimeTable().getStartDay()));
                ps.setDate(2, Date.valueOf(schedule.getScheduleTimeTable().getEndDay()));
                ps.setString(3, schedule.getScheduleTimeTable().getHourFormat().name());
                ps.setTime(4, Time.valueOf(schedule.getScheduleTimeTable().getStartTime()));
                ps.setInt(5, schedule.getScheduleTimeTable().getProgressTime());
                ps.setString(6, schedule.getOrganizationName());
                ps.setString(7, schedule.getAddress().getSido());
                ps.setString(8, schedule.getAddress().getSigungu());
                ps.setString(9, schedule.getAddress().getDetails());
                ps.setString(10, schedule.getAddress().getFullName());
                ps.setString(11, schedule.getContent());
                ps.setInt(12, schedule.getVolunteerNum());
                ps.setInt(13, schedule.getCurrentVolunteerNum());
                ps.setString(14, schedule.getIsDeleted().name());
                ps.setLong(15, schedule.getRecruitment().getRecruitmentNo());
                ps.setTimestamp(16, Timestamp.valueOf(LocalDateTime.now()));
                ps.setTimestamp(17, Timestamp.valueOf(LocalDateTime.now()));
            }

            @Override
            public int getBatchSize() {
                return schedules.size();
            }
        });
    }

}
