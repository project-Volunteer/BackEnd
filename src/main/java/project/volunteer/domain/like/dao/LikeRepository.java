package project.volunteer.domain.like.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import project.volunteer.domain.like.domain.Like;
import project.volunteer.global.common.component.RealWorkCode;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {

    @Query("select l " +
            "from Like l " +
            "where l.user.userNo=:userNo " +
            "and l.realWorkCode=:code " +
            "and l.no=:no ")
    Optional<Like> findLike(@Param("userNo")Long userNo, @Param("code")RealWorkCode code, @Param("no")Long no);

}
