package project.volunteer.domain.sehedule.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import project.volunteer.domain.sehedule.domain.Schedule;

import javax.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import project.volunteer.global.common.component.IsDeleted;

public interface ScheduleRepository extends JpaRepository<Schedule, Long>, ScheduleQueryDSLRepository {

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Schedule s SET s.isDeleted = :isDeleted WHERE s.recruitment.recruitmentNo = :recruitmentNo")
    void bulkUpdateIsDeleted(@Param("isDeleted") IsDeleted isDeleted, @Param("recruitmentNo") Long recruitmentNo);

    @Query("SELECT s " +
            "FROM Schedule s " +
            "WHERE s.scheduleNo=:no " +
            "AND s.isDeleted=project.volunteer.global.common.component.IsDeleted.N")
    Optional<Schedule> findNotDeletedSchedule(@Param("no") Long scheduleNo);






    List<Schedule> findByRecruitment_RecruitmentNo(Long recruitmentNo);

    //삭제되지 않은 일정 검색

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


}
