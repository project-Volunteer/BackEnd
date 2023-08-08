package project.volunteer.domain.reply.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import project.volunteer.domain.reply.domain.Reply;
import project.volunteer.global.common.component.RealWorkCode;


public interface ReplyRepository extends JpaRepository<Reply, Long>{
	
	@Query("select r " +
	"from Reply r " +
	"where r.replyNo=:parentNo")
	Optional<Reply> findVaildParentReply(@Param("parentNo") Long parentNo);

	@Query("select r " +
	"from Reply r " +
	"where r.realWorkCode=:realWorkCode " +
	"and r.no=:no "+
	"and r.replyNo=:replyNo")
	Optional<Reply> findReply(@Param("realWorkCode") RealWorkCode code,@Param("no") Long no, @Param("replyNo") Long replyNo);

	@Query("select r " +
	"from Reply r " +
	"where r.realWorkCode=:realWorkCode " +
	"and r.no=:no")
	List<Reply> findReplyList(@Param("realWorkCode") RealWorkCode code,@Param("no") Long no);

	//테스트 쿼리
	@Query("select p " +
			"from Reply p " +
			"join p.writer u " +
			"left join Image img " +
			"on img.realWorkCode=project.volunteer.global.common.component.RealWorkCode.USER " +
			"and img.no=u.userNo and img.isDeleted=project.volunteer.global.common.component.IsDeleted.N " +
			"left join img.storage sto " +
			"left join p.parent c " +
			"where p.realWorkCode=:realWorkCode and p.no=:no " +
			"order by c.replyNo asc, p.replyNo asc ")
	List<Reply> findCommentReplyList(@Param("realWorkCode") RealWorkCode code,@Param("no") Long no);
}
