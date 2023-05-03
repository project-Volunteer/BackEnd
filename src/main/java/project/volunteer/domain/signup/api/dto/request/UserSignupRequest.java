package project.volunteer.domain.signup.api.dto.request;

import java.time.LocalDate;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;
import project.volunteer.domain.user.domain.Gender;

@Getter @Setter
public class UserSignupRequest {
	@NotBlank(message = "닉네임은 필수 입력 값입니다.")
	private String nickName;
	
	@NotBlank(message = "프로필 사진은 필수 입력 값입니다.")
	private String profile;
	
	@NotBlank(message = "이메일은 필수 입력 값입니다.")
    @Email(message = "올바르지 않은 이메일 형식입니다.")
	private String email;

	@NotNull(message = "생년월일은 필수 입력 값입니다.")
	private LocalDate birthday;
	
	@NotNull(message = "성별은 필수 입력 값입니다.")
	private Gender gender;
	
	private Boolean beforealarm_yn;
    private Boolean joinAlarmYn;
    private Boolean noticeAlarmYn;
    private Boolean beforeAlarmYn;
    private String provider;

	@NotBlank(message = "제공기관 고유키는 필수 입력 값입니다.")
	private String providerId;
	
	public UserSignupRequest() {}
	
	public UserSignupRequest(String nickName, String profile, String email, String birthday, int gender,
			Boolean beforealarm_yn, Boolean joinAlarmYn, Boolean noticeAlarmYn, Boolean beforeAlarmYn, String provider,
			String providerId) {
		this.nickName = nickName;
		this.profile = profile;
		this.email = email;
		this.birthday =  LocalDate.parse(birthday);
		this.gender = gender > 0 ? Gender.M : Gender.W;
		this.beforealarm_yn = beforealarm_yn;
		this.joinAlarmYn = joinAlarmYn;
		this.noticeAlarmYn = noticeAlarmYn;
		this.beforeAlarmYn = beforeAlarmYn;
		this.provider = provider;
		this.providerId = providerId;
	}
}
