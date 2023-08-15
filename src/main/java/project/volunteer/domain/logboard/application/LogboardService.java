package project.volunteer.domain.logboard.application;

import project.volunteer.domain.logboard.application.dto.LogboardDetail;
import project.volunteer.domain.logboard.application.dto.LogboardEditDetail;

public interface LogboardService {
	public Long addLog(Long userNo, String content, Long scheduleNo, Boolean isPublished);

	public LogboardEditDetail findLogboard(Long logboardNo);

	public void editLog(Long logboardNo, Long userNo, String content, Long scheduleNo, Boolean isPublished);

	public void deleteLog(Long userNo, Long logboardNo);

    LogboardDetail detailLog(Long no);

    void likeLogboard(Long loginUserNo, Long no);
}