package project.volunteer.domain.user.api.dto.request;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserAlarmRequestParam {

	@NotNull
	private Boolean joinAlarm;
	
	@NotNull
	private Boolean noticeAlarm;
	
	@NotNull
	private Boolean beforeAlarm;
}
