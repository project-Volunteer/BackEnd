package project.volunteer.domain.reply.application;

import project.volunteer.domain.reply.application.dto.CommentDetails;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.RealWorkCode;

import java.util.List;

public interface ReplyService {
	public Long addComment(User user, RealWorkCode code, Long no, String content);

	public Long addCommentReply(User user, RealWorkCode code, Long no, Long parentNo, String content);

	public void editReply(Long replyNo, String content);

	public void deleteReply(Long replyNo);

	public List<CommentDetails> getCommentReplyList(RealWorkCode code, Long no);
}