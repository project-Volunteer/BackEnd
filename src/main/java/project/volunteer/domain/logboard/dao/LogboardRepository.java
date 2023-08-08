package project.volunteer.domain.logboard.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import project.volunteer.domain.logboard.domain.Logboard;

import java.util.List;

public interface LogboardRepository extends JpaRepository<Logboard, Long>, CustomLogboardRepository {

    @Query("select l " +
            "from Logboard l " +
            "where l.writer.userNo=:loginUserNo " +
            "and l.isDeleted=project.volunteer.global.common.component.IsDeleted.N " +
            "and l.isPublished=:isPublished")
    List<Logboard> findLogboardListByUserNoAndPublishedYn(@Param("loginUserNo")Long loginUserNo, @Param("isPublished") boolean isPublished);
}
