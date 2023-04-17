package project.volunteer.global.jwt.application;

import project.volunteer.global.jwt.dto.JwtToken;

public interface JwtService {
	public JwtToken login(Long userNo, String nickName);
	public JwtToken reissue(String refreshToken);
}
