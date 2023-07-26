package project.volunteer.domain.signup.api.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.volunteer.domain.signup.dto.KakaoUserInfo;
import project.volunteer.global.common.dto.BaseResponse;

@Getter
@Setter
@NoArgsConstructor
public class KakaoUserInfoResponse extends BaseResponse{
	private KakaoUserInfo data;
	
	public KakaoUserInfoResponse(String message, KakaoUserInfo kakaoUserInfo) {
		super(message);
		this.data = kakaoUserInfo;
	}
}
