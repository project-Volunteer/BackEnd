package project.volunteer.global.jwt.application;

import java.util.Optional;

import project.volunteer.domain.user.domain.User;

public interface RefreshTokenService {
	public void updateRefreshToken(String userId, String refreshToken);
	public Optional<User> findById(String userId);
	public Optional<User> findByRefreshToken(String refreshToken);
	public void validRefreshTokenValue(String userId, String refreshToken) throws IllegalAccessException;
}
