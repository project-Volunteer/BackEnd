package project.volunteer.domain.signup.api;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import javax.mail.MessagingException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.netty.util.internal.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.volunteer.domain.signup.api.dto.request.UserEmailRequest;
import project.volunteer.domain.signup.api.dto.request.UserSignupRequest;
import project.volunteer.domain.signup.api.dto.response.KakaoUserInfoResponse;
import project.volunteer.domain.signup.api.dto.response.MailSendResultResponse;
import project.volunteer.domain.signup.api.dto.response.UserSaveResponse;
import project.volunteer.domain.signup.application.KakaoLoginService;
import project.volunteer.domain.signup.application.MailSendService;
import project.volunteer.domain.signup.application.UserSignupService;
import project.volunteer.domain.signup.dto.KakaoUserInfo;

@Slf4j
@Controller
@RequiredArgsConstructor
public class SignupController {
	private final KakaoLoginService kakaoLoginService;
	private final MailSendService mailSendService;
	private final UserSignupService userSignupService;
	
	
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
	
	@ResponseBody
	@PostMapping("/signup")
	public ResponseEntity<KakaoUserInfoResponse> kakaoDataPost(@RequestBody HashMap<String, String> param) throws JsonProcessingException {
		// 회원 가입 체크
		String accessToken = kakaoLoginService.getKakaoAccessToken(param.get("authorizationCode"));
		KakaoUserInfo kakaoUserInfo = kakaoLoginService.getKakaoUserInfo(accessToken);
		
		if(userSignupService.checkDuplicatedUser("kakao_"+kakaoUserInfo.getProviderId())) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(new KakaoUserInfoResponse("이미 가입된 유저입니다.", kakaoUserInfo));
		} else {
			return ResponseEntity.ok(new KakaoUserInfoResponse("success search kakao user info", kakaoUserInfo));
		}
		
	}

	@ResponseBody
	@PostMapping("/signup/email")
	public ResponseEntity<MailSendResultResponse> sendSingUpCode(@RequestBody @Validated UserEmailRequest userEmailRequest) throws MessagingException {
		// 랜덤 키 생성
		String authCode= String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
		
		return mailSendService.sendEmail(userEmailRequest.getEmail(), "Volunteer Sign Auth code", authCode);
	}

	@ResponseBody
	@PostMapping("/signup/user")
	public ResponseEntity<UserSaveResponse> signup(@RequestBody @Validated UserSignupRequest userSignupRequest) {
		Long userNo = userSignupService.addUser(userSignupRequest);
		
		return ResponseEntity.status(HttpStatus.CREATED).body(new UserSaveResponse("success save user", userNo));
	}

}