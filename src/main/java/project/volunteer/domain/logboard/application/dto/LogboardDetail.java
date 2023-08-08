package project.volunteer.domain.logboard.application.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.volunteer.domain.logboard.domain.Logboard;

@Getter
@Setter
@NoArgsConstructor
public class LogboardDetail {
	private List<String> picture = new ArrayList<>();
	private String content;
	private Long scheduleNo;
	
	public LogboardDetail(Logboard logboard) {
        this.content = logboard.getContent();
        this.scheduleNo = logboard.getSchedule().getScheduleNo();
	}
	
	public void setPicture(List<String> picture) {
		this.picture = picture;
	}
}
