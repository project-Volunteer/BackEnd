package project.volunteer.domain.signup.api.dto.request;

import javax.validation.constraints.Email;
import javax.validation.constraints.Future;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import lombok.Data;

@Data
public class UserSignupRequest {
	@NotBlank(message = "필수 입력값입니다.")
	private String nickName;
	@NotBlank(message = "필수 입력값입니다.")
	private String profile;
	
	@NotBlank(message = "필수 입력 값입니다.")
    @Email(message = "이메일 형식에 맞지 않습니다.")
	private String email;
	
	@Pattern(regexp = "^([12]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01]))$", message = "날짜 포맷이 맞지 않습니다.")
	private String birthday;
	
	@Min(0)
	@Max(1)
	private int gender;
	private Boolean beforealarm_yn;
    private Boolean joinAlarmYn;
    private Boolean noticeAlarmYn;
    private Boolean beforeAlarmYn;
    private String provider;
	private String providerId;
	
	public UserSignupRequest() {}
	
	public UserSignupRequest(String nickName, String profile, String email, String birthday, int gender,
			Boolean beforealarm_yn, Boolean joinAlarmYn, Boolean noticeAlarmYn, Boolean beforeAlarmYn, String provider,
			String providerId) {
		this.nickName = nickName;
		this.profile = profile;
		this.email = email;
		this.birthday = birthday;
		this.gender = gender;
		this.beforealarm_yn = beforealarm_yn;
		this.joinAlarmYn = joinAlarmYn;
		this.noticeAlarmYn = noticeAlarmYn;
		this.beforeAlarmYn = beforeAlarmYn;
		this.provider = provider;
		this.providerId = providerId;
	}
}
