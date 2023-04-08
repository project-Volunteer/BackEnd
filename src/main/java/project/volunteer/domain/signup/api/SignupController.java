package project.volunteer.domain.signup.api;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.volunteer.domain.signup.api.dto.request.UserSignupDTO;
import project.volunteer.domain.signup.api.dto.response.KakaoUserInfoResponse;
import project.volunteer.domain.signup.application.KakaoLoginService;
import project.volunteer.domain.signup.application.MailSendService;
import project.volunteer.domain.signup.application.UserSignupService;

@Slf4j
@RestController
@RequiredArgsConstructor
public class SignupController {
	private final KakaoLoginService kakaoLoginService;
	private final MailSendService mailSendService;
	private final UserSignupService userSignupService;
	
	/* 추후 삭제
	// 리턴 authorizationCode 테스트용 
	@GetMapping("/")
	public String root() {
		return "index";
	}
	
	// 리턴 authorizationCode 테스트용
	@ResponseBody
	@GetMapping("/oauth/callback/kakao")
	public String redircturl(@RequestParam String code) {
		
		return code;
	}
	*/
	
	@PostMapping("/signup")
	public KakaoUserInfoResponse kakaoDataPost(@RequestBody HashMap<String, String> param) throws JsonProcessingException {
		// 회원 가입 체크
		String accessToken = kakaoLoginService.getKakaoAccessToken(param.get("authorizationCode"));
		
		return kakaoLoginService.getKakaoUserInfo(accessToken);
	}
	
	@PostMapping("/signup/email")
	public Map<String, String> sendSingUpCode(@RequestBody HashMap<String, String> param) {
		// 랜덤 키 생성
		String authCode= String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
		return mailSendService.sendEmail(param.get("email"), "Volunteer Sign Auth code", authCode);
	}
	
	@PostMapping("/signup/user")
	public String signup(@RequestBody @Validated UserSignupDTO userSignupDTO ) {
		// DB 등록 JPA 활용
		userSignupService.addUser(userSignupDTO);
		
		// 어떤 response메세지 담아서 보낼지 미정
		return "";
	}

}