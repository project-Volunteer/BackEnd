package project.volunteer.domain.security;

import java.io.IOException;
import java.util.function.Predicate;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.volunteer.domain.jwt.application.JwtService;
import project.volunteer.domain.jwt.dto.JwtToken;
import project.volunteer.domain.signup.api.dto.response.SaveUserResponse;
import project.volunteer.domain.signup.dto.SaveUserInfoDTO;
import project.volunteer.domain.user.domain.User;

//"UsernamePasswordCustomFilter" 가 정상적으로 성공할 경우 호출되는 커스텀 Handler => 여기서 JWT 토큰을 반환해준다.
@Slf4j
@Component
@RequiredArgsConstructor
public class UserLoginSuccessCustomHandler implements AuthenticationSuccessHandler {

	private final JwtService jwtService;
	private final ObjectMapper objectMapper;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {

		log.info("로그인 성공");

		// 1. 로그인 인증을 마친 사용자를 authentication에서 가져오기
		User loginUser = ((PrincipalDetails) authentication.getPrincipal()).getUser();

		// 2. 토큰 생성(사용자 고유번호와 별명으로 생성)
		JwtToken jwtToken = jwtService.login(loginUser.getUserNo(), loginUser.getNickName());
		
		
		// 3. response용 Dto 생성
		SaveUserInfoDTO userInfo = SaveUserInfoDTO.builder()
									.email(loginUser.getEmail())
									.nickName(loginUser.getNickName())
									.profile(loginUser.getPicture())
									.gender(loginUser.getGender().getCode())
									.birthday(loginUser.getBirthDay().toString())
									.build();
		
		SaveUserResponse body = new SaveUserResponse(userInfo);
		
		// 4. response
		String res = objectMapper.writeValueAsString(body);
		response.setHeader("refreshToken", jwtToken.getRefreshToken());
		response.setHeader("accessToken", jwtToken.getAccessToken());

		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.setStatus(HttpStatus.OK.value());
		response.getWriter().write(res);
	}
}