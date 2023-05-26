package project.volunteer.domain.sehedule.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import project.volunteer.domain.sehedule.domain.Schedule;

import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule,Long> ,CustomScheduleRepository {

    public Optional<Schedule> findByRecruitment_RecruitmentNo(Long recruitmentNo);

    //삭제되지 않은 일정 검색
    @Query("select s " +
            "from Schedule s " +
            "where s.scheduleNo=:no " +
            "and s.isDeleted=project.volunteer.global.common.component.IsDeleted.N")
    public Optional<Schedule> findValidByScheduleNo(@Param("no") Long scheduleNo);

}
