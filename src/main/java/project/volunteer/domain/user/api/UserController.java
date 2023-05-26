package project.volunteer.domain.user.api;

import javax.validation.Valid;

import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import project.volunteer.domain.user.api.dto.request.UserAlarmRequestParam;
import project.volunteer.domain.user.api.dto.request.UserInfoRequestParam;
import project.volunteer.domain.user.api.dto.response.UserAlarmResponse;
import project.volunteer.domain.user.api.dto.response.UserJoinRequestListResponse;
import project.volunteer.domain.user.api.dto.response.UserRecruitingListResponse;
import project.volunteer.domain.user.application.UserService;
import project.volunteer.global.util.SecurityUtil;

@RestController
@RequiredArgsConstructor
public class UserController {
	private final UserService userService;
	
	@GetMapping("/user/request")
	public HttpEntity<UserJoinRequestListResponse> myJoinRequestList() {
		Long userNo = SecurityUtil.getLoginUserNo();
		return ResponseEntity.ok(userService.findUserJoinRequest(userNo));
	}
	
	@GetMapping("/user/recruiting")
	public HttpEntity<UserRecruitingListResponse> myRecruitingList() {
		Long userNo = SecurityUtil.getLoginUserNo();
		return ResponseEntity.ok(userService.findUserRecruiting(userNo));
	}
	
	@GetMapping("/user/alarm")
	public ResponseEntity<UserAlarmResponse> myAlarm() {
		Long userNo = SecurityUtil.getLoginUserNo();
		return ResponseEntity.ok(userService.findUserAlarm(userNo));
	}
	
	@PutMapping("/user/alarm")
	public ResponseEntity myAlarmUpdate(@RequestBody @Valid UserAlarmRequestParam dto) {
		Long userNo = SecurityUtil.getLoginUserNo();
		userService.userAlarmUpdate(userNo, dto.getJoinAlarm(), dto.getNoticeAlarm(), dto.getBeforeAlarm());

        return ResponseEntity.ok().build();
	}
	
    @PostMapping(value = "/user", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
	public ResponseEntity myInfoUpdate(@ModelAttribute @Valid UserInfoRequestParam dto) {
		Long userNo = SecurityUtil.getLoginUserNo();
		userService.userInfoUpdate(userNo, dto.getNickName(), dto.getEmail(), dto.getProfile());
        return ResponseEntity.ok().build();
	}
	
	
}
