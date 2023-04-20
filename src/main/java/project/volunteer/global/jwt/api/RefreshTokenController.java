package project.volunteer.global.jwt.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import project.volunteer.global.jwt.application.JwtService;
import project.volunteer.global.jwt.dto.JwtToken;

@RequiredArgsConstructor
@RestController
public class RefreshTokenController {
	private final JwtService jwtService;

	@PostMapping("/reissue")
	public ResponseEntity<JwtToken> reissue(@CookieValue("refreshToken") String refreshToken) {
		JwtToken jwtToken = jwtService.reissue(refreshToken);
		
		return ResponseEntity.ok(jwtToken);
	}

}
