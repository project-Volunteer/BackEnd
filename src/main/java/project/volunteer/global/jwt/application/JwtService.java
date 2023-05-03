package project.volunteer.global.jwt.application;

import project.volunteer.global.jwt.dto.JwtToken;

public interface JwtService {
	public JwtToken login(String id);
	public String reissue(String refreshToken) throws IllegalAccessException;
}
