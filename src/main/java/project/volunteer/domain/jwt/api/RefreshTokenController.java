package project.volunteer.domain.jwt.api;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import project.volunteer.domain.jwt.application.JwtService;
import project.volunteer.domain.jwt.dto.JwtToken;

@RequiredArgsConstructor
@RestController
public class RefreshTokenController {
	
	private final JwtService jwtService;

	// 미정
	@PostMapping("/refreshToken")
	public String recreateToken(@RequestHeader("refreshToken") String refreshToken) {
		JwtToken jwtToken = jwtService.reissue(refreshToken);
		
		return "";
	}

}
