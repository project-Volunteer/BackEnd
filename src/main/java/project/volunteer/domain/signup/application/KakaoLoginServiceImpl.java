package project.volunteer.domain.signup.application;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.volunteer.domain.signup.api.dto.response.KakaoUserInfoResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoLoginServiceImpl implements KakaoLoginService{
	@Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
	private String secretkey; 
	
	public String getKakaoAccessToken(String code) throws JsonProcessingException {
		// HTTP Header 생성
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
		headers.add("Accept", "application/json");

		// HTTP Body 생성
		MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
		body.add("grant_type", "authorization_code");
		body.add("client_id", "2b87a8fac130b0d32824ea99c3cff012");
		body.add("client_secret", secretkey);
		body.add("redirect_uri", "http://localhost:8888/oauth/callback/kakao");
		body.add("code", code);

		// HTTP 요청 보내기
		HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest = new HttpEntity<>(body, headers);
		RestTemplate rt = new RestTemplate();
		ResponseEntity<String> response = rt.exchange(
				"https://kauth.kakao.com/oauth/token", 
				HttpMethod.POST,
				kakaoTokenRequest, 
				String.class);

		// HTTP 응답 (JSON) -> 액세스 토큰 파싱
		String responseBody = response.getBody();
		log.info("getKakaoAccessToken() : " +responseBody);
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode jsonNode = objectMapper.readTree(responseBody);
		return jsonNode.get("access_token").asText();
	}

	public KakaoUserInfoResponse getKakaoUserInfo(String accessToken) throws JsonProcessingException {
		// HTTP Header 생성
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + accessToken);
		headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

		// HTTP 요청 보내기
		HttpEntity<MultiValueMap<String, Object>> kakaoUserInfoRequest = new HttpEntity<>(headers);
		RestTemplate rt = new RestTemplate();
		ResponseEntity<String> response = rt.exchange("https://kapi.kakao.com/v2/user/me", HttpMethod.POST, kakaoUserInfoRequest, String.class);

		// responseBody에 있는 정보를 꺼냄
		String responseBody = response.getBody();
		log.info("getKakaoUserInfo() : " +responseBody);
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode jsonNode = objectMapper.readTree(responseBody);

		String providerId = jsonNode.get("id").asText();
		String nickname = jsonNode.get("properties").get("nickname").asText();
		String profile = jsonNode.get("properties").get("profile_image").asText();
		
		KakaoUserInfoResponse kakaoUserInfoResponse = new KakaoUserInfoResponse();
		kakaoUserInfoResponse.setProviderId(providerId);
		kakaoUserInfoResponse.setNickName(nickname);
		kakaoUserInfoResponse.setProfile(profile);
		
		return kakaoUserInfoResponse;
	}
}