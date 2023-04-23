package project.volunteer.domain.signup.application;

import com.fasterxml.jackson.core.JsonProcessingException;

import project.volunteer.domain.signup.api.dto.response.KakaoUserInfoResponse;
import project.volunteer.domain.signup.dto.KakaoUserInfo;

public interface KakaoLoginService {
	public String getKakaoAccessToken(String code) throws JsonProcessingException;
	public KakaoUserInfo getKakaoUserInfo(String accessToken) throws JsonProcessingException ;
	public String getKakaoProviderId(String kakaoAccessToken) throws JsonProcessingException ;
}