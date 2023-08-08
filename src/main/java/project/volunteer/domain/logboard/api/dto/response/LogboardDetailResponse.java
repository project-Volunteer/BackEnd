package project.volunteer.domain.logboard.api.dto.response;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.volunteer.domain.logboard.application.dto.LogboardDetail;

@Getter
@Setter
@NoArgsConstructor
public class LogboardDetailResponse {
	private List<String> picture = new ArrayList<>();
	private String content;
	private Long scheduleNo;
	
	public LogboardDetailResponse(LogboardDetail logboardDetail) {
        this.content = logboardDetail.getContent();
        this.scheduleNo = logboardDetail.getScheduleNo();
        this.picture = logboardDetail.getPicture();
	}
}
