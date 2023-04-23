package project.volunteer.global.jwt.application;

import java.security.InvalidKeyException;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.nimbusds.oauth2.sdk.dpop.verifiers.AccessTokenValidationException;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.jwt.dto.JwtToken;
import project.volunteer.global.jwt.util.JwtProvider;
import project.volunteer.global.util.SecurityUtil;

/**
 * jwt 토큰과 관련된 기능을 포함한 서비스
 */
@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {
	private final RefreshTokenService refreshTokenService;
	private final JwtProvider jwtProvider;

	// 토큰 생성 및 리프레시토큰 DB 저장
	public JwtToken login(String userId) {
		// 토큰 생성
		JwtToken createToken = jwtProvider.createJwtToken(userId);
		Optional<User> findUser = refreshTokenService.findById(userId);
		
		// 리프레시 토큰 갱신
		refreshTokenService.updateRefreshToken(userId, createToken.getRefreshToken());
		
		return createToken;
	}
	
	// 리프레쉬토큰을 통해 accessToken 재발급
	public String reissue(String refreshToken) throws IllegalAccessException{
		Optional<User> findUser = refreshTokenService.findByRefreshToken(refreshToken);

		if (!jwtProvider.validationToken(refreshToken))
			throw new IllegalAccessException();
		
		if(!SecurityUtil.getLoginUserId().equals(findUser.get().getId())){
			throw new IllegalAccessException();
		}
		// 엑세스 토큰 재생성
		String accessToken = jwtProvider.createAccessToken(findUser.get().getId());

		return accessToken;
	}
}