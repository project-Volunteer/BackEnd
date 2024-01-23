package project.volunteer.domain.sehedule.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.sehedule.dao.CustomScheduleRepository;
import project.volunteer.domain.sehedule.domain.Schedule;

import javax.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule,Long> , CustomScheduleRepository {

    List<Schedule> findByRecruitment_RecruitmentNo(Long recruitmentNo);

    //삭제되지 않은 일정 검색
    @Query("select s " +
            "from Schedule s " +
            "where s.scheduleNo=:no " +
            "and s.isDeleted=project.volunteer.global.common.component.IsDeleted.N")
    public Optional<Schedule> findValidSchedule(@Param("no") Long scheduleNo);
    @Query("select s " +
            "from Schedule s " +
            "where s.scheduleNo=:no " +
            "and s.isDeleted=project.volunteer.global.common.component.IsDeleted.N")
    @Lock(LockModeType.OPTIMISTIC) //낙관적 락 사용
    public Optional<Schedule> findValidScheduleWithOPTIMSTICLock(@Param("no") Long scheduleNo);
    @Query("select s " +
            "from Schedule s " +
            "where s.scheduleNo=:no " +
            "and s.isDeleted=project.volunteer.global.common.component.IsDeleted.N")
    @Lock(LockModeType.PESSIMISTIC_WRITE) //비관적 락 사용
    public Optional<Schedule> findValidScheduleWithPESSIMISTIC_WRITE_Lock(@Param("no") Long scheduleNo);


    @Query("select s " +
            "from Schedule s " +
            "where s.recruitment =:recruitment " +
            "and s.isDeleted=project.volunteer.global.common.component.IsDeleted.N " +
            "and s.scheduleTimeTable.startDay between :startDay and :endDay " +
            "order by s.scheduleTimeTable.startDay asc ")
    public List<Schedule> findScheduleWithinRecruitmentPeriod(@Param("recruitment") Recruitment recruitment, @Param("startDay")LocalDate startDay, @Param("endDay")LocalDate endDay);

    @Query("select s " +
            "from Schedule s " +
            "where s.scheduleTimeTable.endDay < current_date " +
            "and s.isDeleted=project.volunteer.global.common.component.IsDeleted.N")
    List<Schedule> findCompletedSchedule();

}
