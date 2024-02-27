package project.volunteer.domain.recruitment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.volunteer.domain.recruitment.domain.repeatPeriod.RepeatPeriod;

import java.util.List;

public interface RepeatPeriodRepository extends JpaRepository<RepeatPeriod, Long> {

    List<RepeatPeriod> findByRecruitment_RecruitmentNo(Long recruitmentNo);

}
