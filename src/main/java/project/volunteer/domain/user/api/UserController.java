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
import project.volunteer.global.util.SecurityUtil;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
	private final UserService userService;
	
	@GetMapping("/request")
	public HttpEntity<UserJoinRequestListResponse> myJoinRequestList() {
		Long userNo = SecurityUtil.getLoginUserNo();
		return ResponseEntity.ok(userService.findUserJoinRequest(userNo));
	}
	
	@GetMapping("/recruiting")
	public HttpEntity<UserRecruitingListResponse> myRecruitingList() {
		Long userNo = SecurityUtil.getLoginUserNo();
		return ResponseEntity.ok(userService.findUserRecruiting(userNo));
	}
	
	
}
