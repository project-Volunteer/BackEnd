package project.volunteer.domain.signup.application;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import project.volunteer.domain.signup.api.dto.response.KakaoUserInfoResponse;
import project.volunteer.domain.signup.dto.KakaoUserInfo;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoLoginServiceImpl implements KakaoLoginService{
	@Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
	private String secretkey; 
	
	public String getKakaoAccessToken(String code) throws JsonProcessingException {
		// 카카오에 보낼 api
		WebClient webClient = WebClient.builder()
				.baseUrl("https://kauth.kakao.com")
				.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.build();
		// 카카오 서버에 요청 보내기 & 응답 받기
		JSONObject response = webClient.post()
				.uri(uriBuilder -> uriBuilder.path("/oauth/token")
						.queryParam("grant_type", "authorization_code")
						.queryParam("client_id", "2b87a8fac130b0d32824ea99c3cff012")
						.queryParam("redirect_uri", "http://localhost:8888/oauth/callback/kakao")
						.queryParam("client_secret", secretkey).queryParam("code", code).build())
				.retrieve().bodyToMono(JSONObject.class).block();

		return (String) response.get("access_token");
	}

	public KakaoUserInfo getKakaoUserInfo(String accessToken) throws JsonProcessingException {
		// 카카오에 요청 보내기 및 응답 받기
		WebClient webClient = WebClient.builder()
				.baseUrl("https://kapi.kakao.com")
				.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.build();

		JSONObject response = webClient.post()
				.uri(uriBuilder -> uriBuilder.path("/v2/user/me").build())
				.header("Authorization", "Bearer " + accessToken)
				.retrieve().bodyToMono(JSONObject.class).block();
		
		String providerId = response.getAsString("id");

		Map<String, Object> kakaoMap = (HashMap<String, Object>) response.get("properties");
		String nickname = (String) kakaoMap.get("nickname");
		String profile = (String) kakaoMap.get("profile_image");
		
		
		return new KakaoUserInfo(nickname, profile, providerId);
	}

	@Override
	public String getKakaoProviderId(String kakaoAccessToken) throws JsonProcessingException {
		WebClient webClient = WebClient.builder()
				.baseUrl("https://kapi.kakao.com")
				.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.build();

		JSONObject response = webClient.post()
				.uri(uriBuilder -> uriBuilder.path("/v2/user/me").build())
				.header("Authorization", "Bearer " + kakaoAccessToken)
				.retrieve().bodyToMono(JSONObject.class).block();
		
		return response.getAsString("id");
	}
}