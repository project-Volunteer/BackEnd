package project.volunteer.global.security.failhandler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.volunteer.global.error.response.BaseErrorResponse;

//"UsernamePasswordCustomFilter" 에서 로그인 실패시 실행되는 커스텀 Handler
@Slf4j
@Component
@RequiredArgsConstructor
public class UserLoginFailureCustomHandler implements AuthenticationFailureHandler {
	private ObjectMapper objectMapper = new ObjectMapper();
	
	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, 
			AuthenticationException exception) throws IOException, ServletException {
		
		log.error("로그인 실패");
		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		response.getWriter().write(objectMapper.writeValueAsString(new BaseErrorResponse("로그인에 실패하셨습니다.")));
	}
}