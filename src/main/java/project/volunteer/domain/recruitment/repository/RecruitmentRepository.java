package project.volunteer.domain.recruitment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import project.volunteer.domain.recruitment.domain.Recruitment;

import java.util.List;
import java.util.Optional;
import project.volunteer.global.common.component.IsDeleted;

public interface RecruitmentRepository extends JpaRepository<Recruitment, Long>, RecruitmentQueryDSLRepository {

    @Query("SELECT r FROM Recruitment r "
            + "WHERE r.recruitmentNo =:no AND r.isDeleted =:isDeleted AND r.isPublished = :isPublished")
    Optional<Recruitment> findRecruitmentBy(@Param("no") Long recruitmentNo, @Param("isDeleted") IsDeleted isDeleted,
                                            @Param("isPublished") Boolean isPublished);

    @Query("SELECT r FROM Recruitment r " +
            "WHERE r.recruitmentNo=:no " +
            "AND r.isDeleted=project.volunteer.global.common.component.IsDeleted.N")
    Optional<Recruitment> findNotDeletedRecruitment(@Param("no") Long recruitmentNo);

    @Query("select r " +
            "from Recruitment r " +
            "where r.writer.userNo=:loginUserNo " +
            "and r.isDeleted=project.volunteer.global.common.component.IsDeleted.N " +
            "and r.isPublished=:isPublished")
    List<Recruitment> findRecruitmentListByUserNoAndPublishedYn(@Param("loginUserNo") Long loginUserNo,
                                                                @Param("isPublished") boolean isPublished);
}
