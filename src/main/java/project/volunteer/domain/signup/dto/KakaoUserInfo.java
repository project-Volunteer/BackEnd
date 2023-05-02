package project.volunteer.domain.signup.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.volunteer.global.common.response.BaseResponse;

@Getter
@Setter
@NoArgsConstructor
public class KakaoUserInfo{
	private String nickName;
	private String profile;
	private String providerId;
	
	public KakaoUserInfo(String nickName, String profile, String providerId) {
		this.nickName = nickName;
		this.profile = profile;
		this.providerId = providerId;
	}
}
