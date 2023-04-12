package project.volunteer.domain.signup.api.dto.response;

import lombok.Data;

@Data
public class KakaoUserInfoResponse {
	private String nickName;
	private String profile;
	private String providerId;
}
