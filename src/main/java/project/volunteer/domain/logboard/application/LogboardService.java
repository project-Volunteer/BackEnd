package project.volunteer.domain.logboard.application;

import javax.validation.Valid;

import project.volunteer.domain.logboard.api.dto.request.AddLogboardCommentParam;
import project.volunteer.domain.logboard.api.dto.request.AddLogboardCommentReplyParam;
import project.volunteer.domain.logboard.api.dto.request.DeleteLogboardReplyParam;
import project.volunteer.domain.logboard.api.dto.request.EditLogboardReplyParam;
import project.volunteer.domain.logboard.application.dto.LogboardDetail;

public interface LogboardService {
	public Long addLog(Long userNo, String content, Long scheduleNo, Boolean isPublished);

	public LogboardDetail findLogboard(Long logboardNo);

	public void editLog(Long logboardNo, Long userNo, String content, Long scheduleNo, Boolean isPublished);

	public void deleteLog(Long userNo, Long logboardNo);

	public Long addLogComment(AddLogboardCommentParam dto, Long loginUserNo);

	public Long addLogCommentReply(AddLogboardCommentReplyParam dto, Long loginUserNo);

	public void editLogReply(EditLogboardReplyParam dto, Long loginUserNo);

	public void deleteLogReply(DeleteLogboardReplyParam dto, Long loginUserNo);

}