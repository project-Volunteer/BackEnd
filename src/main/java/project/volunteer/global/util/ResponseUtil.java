package project.volunteer.global.util;

import java.io.IOException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import project.volunteer.global.jwt.dto.JwtToken;
import project.volunteer.global.security.dto.UserLoginInfo;
import project.volunteer.global.security.dto.UserLoginResponse;


public class ResponseUtil {
	
	public static void setJwtTokenResponseParam(HttpServletResponse response, JwtToken jwtToken, UserLoginInfo userInfo, String message)
			throws JsonProcessingException, IOException {
		// refresh 토큰은 cookie로, accesstoken은 json 페이로드로 전송
		Cookie cookie = new Cookie("refreshToken", jwtToken.getRefreshToken());
		// xss 공격 방어(direct로 브라우저에서 쿠키에 접근할 수 없도록 제한)
		cookie.setHttpOnly(true);
		// https가 아닌 통신에서는 쿠키를 전송하지 않음
		cookie.setSecure(true);
		response.addCookie(cookie);
		
		ObjectMapper objectMapper = new ObjectMapper();
		String res = objectMapper.writeValueAsString(new UserLoginResponse(message, userInfo, jwtToken.getAccessToken()));
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.setStatus(HttpStatus.OK.value());
		response.getWriter().write(res);
	}
}
