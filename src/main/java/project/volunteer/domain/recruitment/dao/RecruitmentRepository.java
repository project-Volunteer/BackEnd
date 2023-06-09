package project.volunteer.domain.recruitment.dao;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import project.volunteer.domain.recruitment.domain.Recruitment;

import java.util.Optional;

public interface RecruitmentRepository extends JpaRepository<Recruitment, Long> {

    //모집글, 작성자 정보 쿼리 한번에 가져오기(left join)
    @EntityGraph(attributePaths = {"writer"})
    @Query("select r from Recruitment r " +
            "where r.recruitmentNo=:no " +
            "and r.isDeleted=project.volunteer.global.common.component.IsDeleted.N " +
            "and r.isPublished=true")
    Optional<Recruitment> findWriterEG(@Param("no") Long recruitmentNo);

    //출판된 게시물(삭제 x, 임시 저장 x)
    @Query("select r " +
            "from Recruitment r " +
            "where r.recruitmentNo=:no " +
            "and r.isDeleted=project.volunteer.global.common.component.IsDeleted.N " +
            "and r.isPublished=true")
    Optional<Recruitment> findPublishedByRecruitmentNo(@Param("no")Long recruitmentNo);

    //삭제되지 않은 게시물
    @Query("select r from Recruitment  r " +
            "where r.recruitmentNo=:no " +
            "and r.isDeleted=project.volunteer.global.common.component.IsDeleted.N")
    Optional<Recruitment> findValidRecruitment(@Param("no") Long recruitmentNo);


    //모집중인 봉사 모집글(삭제 x, 임시 저장 x, 봉사 모집 종료일 내)
    @Query("select r from Recruitment r " +
            "where r.recruitmentNo=:no " +
            "and r.VolunteeringTimeTable.endDay >= current_date " +
            "and r.isDeleted=project.volunteer.global.common.component.IsDeleted.N " +
            "and r.isPublished=true")
    Optional<Recruitment> findActivatedRecruitment(@Param("no") Long recruitmentNo);

}
