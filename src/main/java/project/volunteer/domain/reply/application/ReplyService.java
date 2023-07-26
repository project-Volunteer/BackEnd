package project.volunteer.domain.reply.application;

import project.volunteer.global.common.component.RealWorkCode;

public interface ReplyService {
	public Long addComment(Long loginUserNo, RealWorkCode code, Long no, String content);

	public Long addCommentReply(Long loginUserNo, RealWorkCode code, Long no, Long parentNo, String content);

	public void editReply(Long loginUserNo, Long replyNo, String content);

	public void deleteReply(Long replyNo);

}