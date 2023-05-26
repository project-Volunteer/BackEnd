package project.volunteer.domain.user.api.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserAlarmResponse {
	Boolean joinAlarm;
	Boolean noticeAlarm;
	Boolean beforeAlarm;
	
	public UserAlarmResponse(Boolean joinAlarm, Boolean noticeAlarm, Boolean beforeAlarm) {
		this.joinAlarm = joinAlarm;
		this.noticeAlarm = noticeAlarm;
		this.beforeAlarm = beforeAlarm;
	}
}
