package project.volunteer.domain.sehedule.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import project.volunteer.domain.sehedule.domain.Schedule;

import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule,Long> {

    public Optional<Schedule> findByRecruitment_RecruitmentNo(Long recruitmentNo);

}
