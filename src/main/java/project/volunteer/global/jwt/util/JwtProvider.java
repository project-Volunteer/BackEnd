package project.volunteer.global.jwt.util;

import java.security.Key;
import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.jwt.dto.JwtToken;
import project.volunteer.global.security.PrincipalDetails;

/**
 * 토큰과 관련된 기능
 */
@Slf4j
@Component // 빈으로 등록
public class JwtProvider {
    // 30분
	@Value("${jwt.access-token.expire-length}")
    private long accessTokenValidityInMilliseconds;

    // 14일
    @Value("${jwt.refresh-token.expire-length}")
    private long refreshTokenValidityInMilliseconds; 
    
	public static final String ACCESS_PREFIX_STRING = "Bearer ";
	private final Key key;

	private final UserRepository userRepository;

	public JwtProvider(@Value("${jwt.token.secret-key}") String secret, UserRepository userRepository) {

		byte[] keyBytes = Decoders.BASE64.decode(secret);
		this.key = Keys.hmacShaKeyFor(keyBytes);
		this.userRepository = userRepository;
	}

	// 토큰 생성
	public JwtToken createJwtToken(String userId) {
		// 엑세스 토큰
		String accessToken = ACCESS_PREFIX_STRING
				+ Jwts.builder()
				.setSubject(String.valueOf(userId))
				.claim("userId", userId)
				.setExpiration(new Date(System.currentTimeMillis() + accessTokenValidityInMilliseconds))
				.signWith(key, SignatureAlgorithm.HS512).compact();

		// 리프레시 토큰
		String refreshToken = Jwts.builder()
				.setSubject(String.valueOf(userId) + "_refresh")
				.setExpiration(new Date(System.currentTimeMillis() + refreshTokenValidityInMilliseconds))
				.signWith(key, SignatureAlgorithm.HS512).compact();

		return new JwtToken(accessToken, refreshToken);
	}

	// access token 만 생성 -> refresh 토큰 요청이 왔을때 사용됨
	public String createAccessToken(String userId) {
		return ACCESS_PREFIX_STRING + Jwts.builder()
				.setSubject(String.valueOf(userId))
				.claim("userId", userId)
				.setExpiration(new Date(System.currentTimeMillis() + accessTokenValidityInMilliseconds))
				.signWith(key, SignatureAlgorithm.HS512).compact();
	}

	// 엑세스 토큰에서 인증 정보 객체(Authentication) 생성
	public Authentication getAuthentication(String accessToken) {
		// 이 메서드를 호출하기 이전에 필터에서 토큰 검증은 끝냈으니 Claims를 받아와도 에러가 발생하지 않는다.
		Claims claims = parseClaims(accessToken);

		// 1. 토큰안에 필요한 Claims가 있는지 확인
		if (claims.get("userId") == null)
			return null;

		// 2. DB 에 사용자가 있는지 확인 -> 탈퇴했을 경우를 위해서
		String userId = claims.get("userId").toString();
		Optional<User> findUser = userRepository.findById(userId);
		if (findUser.isEmpty())
			return null;

		UserDetails userDetails = new PrincipalDetails(findUser.get());
		// 추후에 권한 검사를 하기 때문에 credentials는 굳이 필요없음
		return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());

	}

	// 토큰 유효성 검사
	public Boolean validationToken(String token) {
		try {
			Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
		} catch (ExpiredJwtException e) {
			log.error("ExpiredJwtException", e.getMessage());
			return false;
		} catch (MalformedJwtException e) {
			log.error("MalformedJwtException", e.getMessage());
			return false;
		} catch (IllegalArgumentException e) {
			log.error("IllegalArgumentException", e.getMessage());
			return false;
		} catch (Exception e) {
			log.error("Exception", e.getMessage());
			return false;
		}
		return true;
	}

	// 엑세스 토큰의 만료시간
	public Long getExpiration(String accessToken) {
		Date expiration = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody()
				.getExpiration();

		long now = new Date().getTime();
		return expiration.getTime() - now;
	}

	// 토큰 Claims 가져오기
	public Claims parseClaims(String accessToken) {
		return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
	}
}