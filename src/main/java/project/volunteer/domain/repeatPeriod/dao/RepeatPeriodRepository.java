package project.volunteer.domain.repeatPeriod.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import project.volunteer.domain.repeatPeriod.domain.RepeatPeriod;

import java.util.List;

public interface RepeatPeriodRepository extends JpaRepository<RepeatPeriod, Long> {

    List<RepeatPeriod> findByRecruitment_RecruitmentNo(Long recruitmentNo);

}
