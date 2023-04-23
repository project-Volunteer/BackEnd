package project.volunteer.global.security;

import java.io.IOException;
import java.util.function.Predicate;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.jwt.application.JwtService;
import project.volunteer.global.jwt.dto.JwtToken;
import project.volunteer.global.security.dto.UserLoginInfo;
import project.volunteer.global.security.dto.UserLoginResponse;
import project.volunteer.global.util.ResponseUtil;

//"UsernamePasswordCustomFilter" 가 정상적으로 성공할 경우 호출되는 커스텀 Handler => 여기서 JWT 토큰을 반환해준다.
@Slf4j
@Component
@RequiredArgsConstructor
public class UserLoginSuccessCustomHandler implements AuthenticationSuccessHandler {

	private final JwtService jwtService;
	private final ResponseUtil responseUtil;
	
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {

		log.info("로그인 성공");

		// 1. 로그인 인증을 마친 사용자를 authentication에서 가져오기
		User loginUser = ((PrincipalDetails) authentication.getPrincipal()).getUser();

		// 2. 토큰 생성(사용자 고유번호와 별명으로 생성)
		JwtToken jwtToken = jwtService.login(loginUser.getId());
		
		// 3. response용 Dto 생성
		UserLoginInfo userInfo = UserLoginInfo.builder()
									.email(loginUser.getEmail())
									.nickName(loginUser.getNickName())
									.profile(loginUser.getPicture())
									.gender(loginUser.getGender().getCode())
									.birthday(loginUser.getBirthDay().toString())
									.build();
		
		// 4. response
		responseUtil.setJwtTokenResponseParam(response, jwtToken, userInfo, "success login");
	}

	
}