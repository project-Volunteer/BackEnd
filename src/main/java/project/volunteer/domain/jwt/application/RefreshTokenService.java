package project.volunteer.domain.jwt.application;

import java.util.Optional;

import project.volunteer.domain.user.domain.User;

public interface RefreshTokenService {
	public void updateRefreshToken(Long userNo, String refreshToken);
	public Optional<User> findByUserNo(Long userNo);
	public void validRefreshTokenValue(Long userNo, String refreshToken) throws IllegalAccessException;
}
