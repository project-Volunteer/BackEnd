package project.volunteer.global.jwt;

import java.util.Arrays;
import java.util.Optional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerInterceptor;
import lombok.RequiredArgsConstructor;
import project.volunteer.global.jwt.application.RefreshTokenService;
import project.volunteer.global.jwt.util.JwtProvider;
import project.volunteer.global.util.SecurityUtil;

@RequiredArgsConstructor
public class RefreshTokenAuthInterceptor implements HandlerInterceptor {

	private final JwtProvider jwtProvider;
	private final RefreshTokenService refreshTokenService;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		// 1. Request Header 토큰 추출
		String refreshToken = getToken(request);

		// 2. 리프레쉬 토큰 유효성 검사
		if (!jwtProvider.validationToken(refreshToken))
			throw new IllegalAccessException();

		// 3. 로그인 정보
		String userId = SecurityUtil.getLoginUserId();
		if (userId == null)
			throw new NullPointerException();

		// 4. DB의 리프레쉬 토큰과 일치하는지 확인
		refreshTokenService.validRefreshTokenValue(userId, refreshToken);

		return true;
	}

	// Request Header 에서 토큰 추출
	private String getToken(HttpServletRequest request) throws NullPointerException {
		Cookie[] cookies = request.getCookies();
		
		Optional<String> refreshToken = Arrays.stream(cookies)
				.filter(c -> "refreshToken".equals(c.getName()))
				.map(Cookie::getValue)
				.findAny();
		
		refreshToken.orElseThrow(NullPointerException::new);

		return refreshToken.orElse("has not cookie");
	}
}