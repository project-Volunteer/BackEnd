package project.volunteer.domain.sehedule.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import project.volunteer.domain.sehedule.domain.Schedule;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule,Long> ,CustomScheduleRepository {

    public Optional<Schedule> findByRecruitment_RecruitmentNo(Long recruitmentNo);

    //삭제되지 않은 일정 검색
    @Query("select s " +
            "from Schedule s " +
            "where s.scheduleNo=:no " +
            "and s.isDeleted=project.volunteer.global.common.component.IsDeleted.N")
    public Optional<Schedule> findValidSchedule(@Param("no") Long scheduleNo);

    @Query("select s " +
            "from Schedule s " +
            "where s.recruitment.recruitmentNo=:no " +
            "and s.isDeleted=project.volunteer.global.common.component.IsDeleted.N " +
            "and s.scheduleTimeTable.startDay between :startDay and :endDay " +
            "order by s.scheduleTimeTable.startDay asc ")
    public List<Schedule> findScheduleWithinPeriod(@Param("no")Long recruitmentNo, @Param("startDay")LocalDate startDay, @Param("endDay")LocalDate endDay);

    @Query("select s " +
            "from Schedule s " +
            "where s.scheduleNo=:no " +
            "and s.scheduleTimeTable.endDay > current_date " +
            "and s.isDeleted=project.volunteer.global.common.component.IsDeleted.N")
    Optional<Schedule> findActivateSchedule(@Param("no")Long scheduleNo);

}
