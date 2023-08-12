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
public class LogboardEditDetail {
	private List<String> pictures = new ArrayList<>();
	private String content;
	private Long scheduleNo;
	
	public LogboardEditDetail(Logboard logboard) {
        this.content = logboard.getContent();
        this.scheduleNo = logboard.getSchedule().getScheduleNo();
	}
	
	public void setPicture(List<String> pictures) {
		this.pictures = pictures;
	}
}
