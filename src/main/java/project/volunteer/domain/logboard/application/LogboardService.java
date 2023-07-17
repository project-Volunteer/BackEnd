package project.volunteer.domain.logboard.application;

import project.volunteer.domain.logboard.application.dto.LogboardDetail;

public interface LogboardService {
	public Long addLog(Long userNo, String content, Long scheduleNo, Boolean isPublished);

	public LogboardDetail findLogboard(Long logboardNo);

	public void editLog(Long logboardNo, Long userNo, String content, Long scheduleNo, Boolean isPublished);

	public void deleteLog(Long userNo, Long logboardNo);
}