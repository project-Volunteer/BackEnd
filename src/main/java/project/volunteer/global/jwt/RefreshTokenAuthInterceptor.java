package project.volunteer.global.jwt;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import project.volunteer.global.jwt.application.RefreshTokenService;
import project.volunteer.global.jwt.util.JwtProvider;

@RequiredArgsConstructor
public class RefreshTokenAuthInterceptor implements HandlerInterceptor {

	private final JwtProvider jwtProvider;
	private final RefreshTokenService refreshTokenService;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		// 1. Request Header 토큰 추출
		String token = getToken(request);

		// 2. 리프레쉬 토큰 유효성 검사
		if (!jwtProvider.validationToken(token))
			throw new IllegalAccessException();

		// 3. 리프레쉬 토큰에서 Claims 검증 -> 위에서 유효성 검사를 했기 때문에 Claims를 가져오는데는 오류가 발생하지 않음
		Claims claims = jwtProvider.parseClaims(token);
		if (claims.get("userNo") == null)
			throw new NullPointerException();
		Long userNo = Long.valueOf(claims.get("userNo").toString());

		// 4. DB의 리프레쉬 토큰과 일치하는지 확인
		refreshTokenService.validRefreshTokenValue(userNo, token);

		return true;
	}

	// Request Header 에서 토큰 추출
	private String getToken(HttpServletRequest request) throws NullPointerException {
		String token = request.getHeader("refreshToken");
		if (!StringUtils.hasText(token))
			throw new NullPointerException();

		return token;
	}
}