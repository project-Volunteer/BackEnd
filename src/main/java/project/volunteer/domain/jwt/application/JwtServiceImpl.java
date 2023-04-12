package project.volunteer.domain.jwt.application;

import java.util.Optional;

import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import project.volunteer.domain.jwt.dto.JwtToken;
import project.volunteer.domain.jwt.util.JwtProvider;
import project.volunteer.domain.user.domain.User;

/**
 * jwt 토큰과 관련된 기능을 포함한 서비스
 */
@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {
	private final RefreshTokenService refreshTokenService;
	private final JwtProvider jwtProvider;

	// 토큰 생성 및 리프레시토큰 DB 저장
	public JwtToken login(Long userNo, String nickName) {
		// 토큰 생성
		JwtToken createToken = jwtProvider.createJwtToken(userNo, nickName);
		Optional<User> findUser = refreshTokenService.findByUserNo(userNo);
		
		// 리프레시 토큰 갱신
		refreshTokenService.updateRefreshToken(userNo, createToken.getRefreshToken());
		
		return createToken;
	}
	
	// 리프레쉬토큰을 통해 accessToken 재발급
	public JwtToken reissue(String refreshToken) {

		// 리프레쉬 토큰에서 userNo 추출
		Claims claims = jwtProvider.parseClaims(refreshToken);
		Long userNo = Long.valueOf(claims.get("userNo").toString());
		
		
		// User 검색
		Optional<User> findUser = refreshTokenService.findByUserNo(userNo);

		// 엑세스 토큰 재생성
		String accessToken = jwtProvider.createAccessToken(userNo, findUser.get().getNickName());

		return new JwtToken(accessToken, refreshToken);
	}
}