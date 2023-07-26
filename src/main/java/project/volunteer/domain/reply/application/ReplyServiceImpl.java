package project.volunteer.domain.reply.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.logboard.dao.LogboardRepository;
import project.volunteer.domain.logboard.domain.Logboard;
import project.volunteer.domain.reply.dao.ReplyRepository;
import project.volunteer.domain.reply.domain.Reply;
import project.volunteer.domain.scheduleParticipation.dao.ScheduleParticipationRepository;
import project.volunteer.domain.scheduleParticipation.domain.ScheduleParticipation;
import project.volunteer.domain.sehedule.dao.ScheduleRepository;
import project.volunteer.domain.sehedule.domain.Schedule;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.ParticipantState;
import project.volunteer.global.common.component.RealWorkCode;
import project.volunteer.global.common.validate.ReplyValidate;
import project.volunteer.global.common.validate.UserValidate;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReplyServiceImpl implements ReplyService {
	private final ReplyRepository replyRepository;
	private final ReplyValidate replyValidate;
	private final UserValidate userValidate;

	@Override
    @Transactional
	public Long addComment(Long loginUserNo, RealWorkCode code, Long no, String content) {
		// 사용자 존재 유무 검증
		User user = userValidate.validateAndGetUser(loginUserNo);

		// 댓글의 모글(도메인) 검증
		replyValidate.validateRealWorkDomain(code, no);

		Reply reply = Reply.createComment(code, no, content, loginUserNo);
		reply.setWriter(user);
		
		replyRepository.save(reply);
		
		return reply.getReplyNo();
	}
	

	@Override
	@Transactional
	public Long addCommentReply(Long loginUserNo, RealWorkCode code, Long no, Long parentNo, String content) {
		// 사용자 존재 유무 검증
		User user = userValidate.validateAndGetUser(loginUserNo);

		// 대댓글의 모글(도메인) 검증
		replyValidate.validateRealWorkDomain(code, no);
		
		// 부모 댓글 유무 확인
		Reply findComment = replyValidate.validateAndGetParentReply(parentNo);
		
		// 부모 댓글이 1depth 댓글인지 확인
		replyValidate.vaildateParentReplyHasNotParent(findComment);

		Reply reply = Reply.createCommentReply(findComment, RealWorkCode.LOG, no, content, loginUserNo);
		reply.setWriter(user);
		
		return replyRepository.save(reply).getReplyNo();
	}

	@Override
	@Transactional
	public void editReply(Long loginUserNo, Long replyNo, String content) {
		// 댓글 존재 여부 검증
		Reply findReply = replyValidate.validateAndGetReply(replyNo);

		// 작성자 여부 검증
//		replyValidate.vaildateEqualParamUserNoAndReplyFindUserNo(loginUserNo, findReply);

		findReply.editReply(content, loginUserNo);
	}

	@Override
	@Transactional
	public void deleteReply(Long replyNo) {
		// 댓글 존재 여부 검증
		Reply findReply = replyValidate.validateAndGetReply(replyNo);

		replyRepository.delete(findReply);
	}
}
