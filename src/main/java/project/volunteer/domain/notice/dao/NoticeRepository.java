package project.volunteer.domain.notice.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import project.volunteer.domain.notice.domain.Notice;

import javax.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    @Query("select n " +
            "from Notice n " +
            "where n.noticeNo=:no " +
            "and n.isDeleted=project.volunteer.global.common.component.IsDeleted.N")
    Optional<Notice> findValidNotice(@Param("no")Long noticeNo);

    @Query("select n " +
            "from Notice n " +
            "where n.noticeNo=:no " +
            "and n.isDeleted=project.volunteer.global.common.component.IsDeleted.N")
    @Lock(LockModeType.OPTIMISTIC) //낙관적 락 사용
    Optional<Notice> findValidNoticeWithOPTIMSTICLOCK(@Param("no")Long noticeNo);

    List<Notice> findByRecruitment_RecruitmentNo(Long recruitmentNo);

}
