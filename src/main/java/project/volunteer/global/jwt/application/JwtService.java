package project.volunteer.global.jwt.application;

import project.volunteer.global.jwt.dto.JwtToken;

public interface JwtService {
	public JwtToken login(Long userNo);
	public JwtToken reissue(String refreshToken);
}
