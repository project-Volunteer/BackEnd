package project.volunteer.domain.recruitment.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import project.volunteer.domain.recruitment.dao.queryDto.RecruitmentQueryDtoRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;

public interface RecruitmentRepository extends JpaRepository<Recruitment, Long> {


}
