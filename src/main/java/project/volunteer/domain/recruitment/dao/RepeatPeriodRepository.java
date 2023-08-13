package project.volunteer.domain.recruitment.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import project.volunteer.domain.recruitment.domain.RepeatPeriod;

import java.util.List;

public interface RepeatPeriodRepository extends JpaRepository<RepeatPeriod, Long> {

    List<RepeatPeriod> findByRecruitment_RecruitmentNo(Long recruitmentNo);

}
