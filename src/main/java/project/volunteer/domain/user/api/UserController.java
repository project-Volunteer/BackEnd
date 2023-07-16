package project.volunteer.domain.user.api;

import java.util.Optional;

import javax.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import project.volunteer.domain.image.application.ImageService;
import project.volunteer.domain.image.application.dto.ImageParam;
import project.volunteer.domain.image.dao.ImageRepository;
import project.volunteer.domain.image.domain.Image;
import project.volunteer.domain.image.domain.ImageType;
import project.volunteer.domain.user.api.dto.response.*;
import project.volunteer.global.common.component.RealWorkCode;
import project.volunteer.domain.user.api.dto.request.UserAlarmRequestParam;
import project.volunteer.domain.user.api.dto.request.UserInfoRequestParam;
import project.volunteer.domain.user.application.UserDtoService;
import project.volunteer.domain.user.application.UserService;
import project.volunteer.global.util.SecurityUtil;

@RestController
@RequiredArgsConstructor
public class UserController {
	private final UserService userService;
	private final UserDtoService userDtoService;
	private final ImageService imageService;
    private final ImageRepository imageRepository; 
	
	@GetMapping("/user/request")
	public HttpEntity<UserJoinRequestListResponse> myJoinRequestList() {
		return ResponseEntity.ok(userDtoService.findUserJoinRequest(SecurityUtil.getLoginUserNo()));
	}
	
	@GetMapping("/user/recruiting")
	public HttpEntity<UserRecruitingListResponse> myRecruitingList() {
		return ResponseEntity.ok(userDtoService.findUserRecruiting(SecurityUtil.getLoginUserNo()));
	}
	
	@GetMapping("/user/alarm")
	public ResponseEntity<UserAlarmResponse> myAlarm() {
		return ResponseEntity.ok(userDtoService.findUserAlarm(SecurityUtil.getLoginUserNo()));
	}
	
	@PutMapping("/user/alarm")
	public ResponseEntity myAlarmUpdate(@RequestBody @Valid UserAlarmRequestParam dto) {
		userService.userAlarmUpdate(SecurityUtil.getLoginUserNo(), dto.getJoinAlarm(), dto.getNoticeAlarm(), dto.getBeforeAlarm());
        return ResponseEntity.ok().build();
	}
	
    @PostMapping("/user")
	public ResponseEntity myInfoUpdate(@ModelAttribute @Valid UserInfoRequestParam dto) {
		String picture = null;
		
		if(dto.getProfile() != null) {
			imageService.deleteImage(RealWorkCode.USER, SecurityUtil.getLoginUserNo());
			ImageParam uploadUserProfile = new ImageParam(RealWorkCode.USER, SecurityUtil.getLoginUserNo(), ImageType.UPLOAD, null, dto.getProfile());
			imageService.addImage(uploadUserProfile);
			Optional<Image> savedImg = imageRepository.findEGStorageByCodeAndNo(RealWorkCode.USER, SecurityUtil.getLoginUserNo());
			
			picture = savedImg.get().getStorage().getImagePath();
		}
		
		userService.userInfoUpdate(SecurityUtil.getLoginUserNo(), dto.getNickName(), dto.getEmail(), picture);
        return ResponseEntity.ok().build();
	}
	
	@GetMapping("/user/info")
	public ResponseEntity<UserDashboardResponse> myInfo(){
		UserInfo userInfo = userDtoService.findUserInfo(SecurityUtil.getLoginUserNo());
		HistoryTimeInfo historyTimeInfo = userDtoService.findHistoryTimeInfo(SecurityUtil.getLoginUserNo());
		ActivityInfo activityInfo = userDtoService.findActivityInfo(SecurityUtil.getLoginUserNo());

		return ResponseEntity.ok(new UserDashboardResponse(userInfo, historyTimeInfo, activityInfo));
	}


}
