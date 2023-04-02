package project.volunteer.domain.recruitment.dao;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;

import java.util.Optional;

public interface RecruitmentRepository extends JpaRepository<Recruitment, Long> {

    //모집글, 작성자 정보 쿼리 한번에 가져오기(left join)
    @EntityGraph(attributePaths = {"writer"})
    Optional<Recruitment> findEGWriterByRecruitmentNo(Long recruitmentNo);
}
