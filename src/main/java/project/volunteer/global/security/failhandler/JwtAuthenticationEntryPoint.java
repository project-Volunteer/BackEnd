package project.volunteer.global.security.failhandler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.volunteer.global.error.response.BaseErrorResponse;

//Spring Security 에서 인증되지 않은 사용자의 리소스에 대한 접근 처리는 AuthenticationEntryPoint 가 담당
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
	private ObjectMapper objectMapper = new ObjectMapper();
	
	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {

		log.error("JWT 토큰 인증 실패");
		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		response.getWriter().write(objectMapper.writeValueAsString(new BaseErrorResponse("로그인에 실패하셨습니다.")));
	}
}