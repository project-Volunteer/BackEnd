package project.volunteer.domain.signup.application;

import com.fasterxml.jackson.core.JsonProcessingException;

import project.volunteer.domain.signup.api.dto.response.KakaoUserInfoResponse;

public interface KakaoLoginService {
	public String getKakaoAccessToken(String code) throws JsonProcessingException;
	public KakaoUserInfoResponse getKakaoUserInfo(String accessToken) throws JsonProcessingException ;
}