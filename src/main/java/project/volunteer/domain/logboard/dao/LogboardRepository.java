package project.volunteer.domain.logboard.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import project.volunteer.domain.logboard.domain.Logboard;

import javax.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface LogboardRepository extends JpaRepository<Logboard, Long>, CustomLogboardRepository {

    @Query("select l " +
            "from Logboard l " +
            "where l.writer.userNo=:loginUserNo " +
            "and l.isDeleted=project.volunteer.global.common.component.IsDeleted.N " +
            "and l.isPublished=:isPublished")
    List<Logboard> findLogboardListByUserNoAndPublishedYn(@Param("loginUserNo")Long loginUserNo, @Param("isPublished") boolean isPublished);

    @Query("select l " +
            "from Logboard l " +
            "where l.logboardNo=:logboardNo " +
            "and l.isDeleted=project.volunteer.global.common.component.IsDeleted.N")
    @Lock(LockModeType.OPTIMISTIC) //낙관적 락 사용
    Optional<Logboard> findValidLogboardWithOPTIMSTICLOCK(@Param("logboardNo")Long logboardNo);
}
