package project.volunteer.domain.user.api;

import java.util.Optional;

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
import project.volunteer.domain.image.application.ImageService;
import project.volunteer.domain.image.application.dto.ImageParam;
import project.volunteer.domain.image.dao.ImageRepository;
import project.volunteer.domain.image.domain.Image;
import project.volunteer.domain.image.domain.ImageType;
import project.volunteer.domain.image.domain.RealWorkCode;
import project.volunteer.domain.user.api.dto.request.UserAlarmRequestParam;
import project.volunteer.domain.user.api.dto.request.UserInfoRequestParam;
import project.volunteer.domain.user.api.dto.response.UserAlarmResponse;
import project.volunteer.domain.user.api.dto.response.UserJoinRequestListResponse;
import project.volunteer.domain.user.api.dto.response.UserRecruitingListResponse;
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
		Long userNo = SecurityUtil.getLoginUserNo();
		return ResponseEntity.ok(userDtoService.findUserJoinRequest(userNo));
	}
	
	@GetMapping("/user/recruiting")
	public HttpEntity<UserRecruitingListResponse> myRecruitingList() {
		Long userNo = SecurityUtil.getLoginUserNo();
		return ResponseEntity.ok(userDtoService.findUserRecruiting(userNo));
	}
	
	@GetMapping("/user/alarm")
	public ResponseEntity<UserAlarmResponse> myAlarm() {
		Long userNo = SecurityUtil.getLoginUserNo();
		return ResponseEntity.ok(userDtoService.findUserAlarm(userNo));
	}
	
	@PutMapping("/user/alarm")
	public ResponseEntity myAlarmUpdate(@RequestBody @Valid UserAlarmRequestParam dto) {
		Long userNo = SecurityUtil.getLoginUserNo();
		userService.userAlarmUpdate(userNo, dto.getJoinAlarm(), dto.getNoticeAlarm(), dto.getBeforeAlarm());

        return ResponseEntity.ok().build();
	}
	
    @PostMapping("/user")
	public ResponseEntity myInfoUpdate(@ModelAttribute @Valid UserInfoRequestParam dto) {
		Long userNo = SecurityUtil.getLoginUserNo();
		
		String picture = null;
		
		if(dto.getProfile() != null) {
			imageService.deleteImage(RealWorkCode.USER, userNo);
			ImageParam uploadUserProfile = new ImageParam(RealWorkCode.USER, userNo, ImageType.UPLOAD, null, dto.getProfile());
			imageService.addImage(uploadUserProfile);
			Optional<Image> savedImg = imageRepository.findEGStorageByCodeAndNo(RealWorkCode.USER, userNo);
			
			picture = savedImg.get().getStorage().getImagePath();
		}
		
		userService.userInfoUpdate(userNo, dto.getNickName(), dto.getEmail(), picture);
        return ResponseEntity.ok().build();
	}
	
	
}
