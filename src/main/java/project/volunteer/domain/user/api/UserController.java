package project.volunteer.domain.user.api;

import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import project.volunteer.domain.user.api.dto.response.UserJoinRequestListResponse;
import project.volunteer.domain.user.api.dto.response.UserRecruitingListResponse;
import project.volunteer.domain.user.application.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
	private final UserService userService;
	
	@GetMapping("/request")
	public HttpEntity<UserJoinRequestListResponse> myJoinRequestList() {
		
		return ResponseEntity.ok(userService.findUserJoinRequest());
	}
	
	@GetMapping("/recruiting")
	public HttpEntity<UserRecruitingListResponse> myRecruitingList() {
		
		return ResponseEntity.ok(userService.findUserRecruiting());
	}
	
	
}
