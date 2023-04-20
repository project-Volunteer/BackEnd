package project.volunteer.global.security.failhandler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.volunteer.global.common.response.BaseResponse;

@Slf4j
@Component
@RequiredArgsConstructor
//Spring Security 에서 인증이 되었지만 권한이 없는 사용자의 리소스에 대한 접근 처리는 AccessDeniedHandler 가 담당
public class JwtAccessDeniedHandler implements AccessDeniedHandler {
	private ObjectMapper objectMapper = new ObjectMapper();
	
	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException, ServletException {

		log.error("권한이 없는 사용자 접근");
		response.setStatus(HttpStatus.FORBIDDEN.value());
		response.getWriter().write(objectMapper.writeValueAsString(new BaseResponse("not acceptable fail")));
	}
}
