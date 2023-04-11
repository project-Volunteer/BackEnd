package project.volunteer.domain.jwt.application;

import project.volunteer.domain.jwt.dto.JwtToken;

public interface JwtService {
	public JwtToken login(Long userNo, String nickName);
	public JwtToken reissue(String refreshToken);
}
