package project.volunteer.domain.user.api;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import project.volunteer.domain.image.application.ImageService;
import project.volunteer.domain.image.application.dto.ImageParam;
import project.volunteer.domain.image.dao.ImageRepository;
import project.volunteer.domain.image.domain.Image;
import project.volunteer.domain.logboard.application.LogboardService;
import project.volunteer.domain.recruitment.application.RecruitmentCommandUseCase;
import project.volunteer.domain.recruitment.application.RecruitmentQueryService;
import project.volunteer.domain.user.api.dto.request.LogboardListRequestParam;
import project.volunteer.domain.user.api.dto.request.RecruitmentListRequestParam;
import project.volunteer.domain.user.api.dto.response.*;
import project.volunteer.domain.user.dao.queryDto.UserQueryDtoRepository;
import project.volunteer.domain.user.dao.queryDto.dto.UserHistoryQuery;
import project.volunteer.global.Interceptor.OrganizationAuth;
import project.volunteer.global.Interceptor.OrganizationAuth.Auth;
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
	private final RecruitmentCommandUseCase recruitmentCommandUseCase;
	private final RecruitmentQueryService recruitmentQueryService;
	private final LogboardService logboardService;
    private final ImageRepository imageRepository;
	private final UserQueryDtoRepository userQueryDtoRepository;

	@DeleteMapping("/logout")
	public HttpEntity logOut(HttpServletRequest request, HttpServletResponse response) {
		userService.userRefreshTokenUpdate(SecurityUtil.getLoginUserNo(),"");
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if(authentication != null) {
			new SecurityContextLogoutHandler().logout(request,response,authentication);
		}
		return ResponseEntity.ok().build();
	}

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
		return ResponseEntity.ok(userService.findUserAlarm(SecurityUtil.getLoginUserNo()));
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
			ImageParam uploadUserProfile = new ImageParam(RealWorkCode.USER, SecurityUtil.getLoginUserNo(), dto.getProfile());
			imageService.addImage(uploadUserProfile);
			Optional<Image> savedImg = imageRepository.findEGStorageByCodeAndNo(RealWorkCode.USER, SecurityUtil.getLoginUserNo());
			
			picture = savedImg.get().getStorage().getImagePath();
		}
		
		userService.userInfoUpdate(SecurityUtil.getLoginUserNo(), dto.getNickName(), dto.getEmail(), picture);
        return ResponseEntity.ok().build();
	}
	
	@GetMapping("/user/info")
	public ResponseEntity<UserDashboardResponse> myInfo(){
		UserInfo userInfo = userService.findUserInfo(SecurityUtil.getLoginUserNo());
		HistoryTimeInfo historyTimeInfo = userDtoService.findHistoryTimeInfo(SecurityUtil.getLoginUserNo());
		ActivityInfo activityInfo = userDtoService.findActivityInfo(SecurityUtil.getLoginUserNo());

		return ResponseEntity.ok(new UserDashboardResponse(userInfo, historyTimeInfo, activityInfo));
	}

	@GetMapping("/user/history")
	public ResponseEntity<HistoryListResponse> myHistory(@PageableDefault(size = 6) Pageable pageable,
														 @RequestParam(required = false) Long last_id) {
		Slice<UserHistoryQuery> result = userQueryDtoRepository.findHistoryDtos(SecurityUtil.getLoginUserNo(), pageable, last_id);

		//response DTO 변환
		List<HistoriesList> dtos = result.getContent().stream().map(dto -> HistoriesList.makeHistoriesList(dto)).collect(Collectors.toList());
		return ResponseEntity.ok(new HistoryListResponse(dtos, result.isLast(), (dtos.isEmpty())?null:(dtos.get(dtos.size()-1).getNo())));
	}

	@GetMapping("/user/recruitment/temp")
	public ResponseEntity<RecruitmentTempListResponse> myRecruitmentTemp() {
		return ResponseEntity.ok(userDtoService.findRecruitmentTempDtos(SecurityUtil.getLoginUserNo()));
	}

	@GetMapping("/user/logboard/temp")
	public ResponseEntity<LogboardTempListResponse> myLogboardTemp() {
		return ResponseEntity.ok(userDtoService.findLoboardTempDtos(SecurityUtil.getLoginUserNo()));
	}

	@OrganizationAuth(auth = Auth.ORGANIZATION_ADMIN)
	@DeleteMapping ("/user/recruitment/temp")
	public ResponseEntity myRecruitmentTempDelete(@RequestBody @Valid RecruitmentListRequestParam dto) {
		Long userNo = SecurityUtil.getLoginUserNo();
		for(Long recruitmentNo : dto.getRecruitmentList()){
			recruitmentCommandUseCase.deleteRecruitment(recruitmentNo);
		}
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/user/logboard/temp")
	public ResponseEntity myLogboardTempDelete(@RequestBody @Valid LogboardListRequestParam dto) {
		for(Long recruitmentNo : dto.getLogboardList()){

			logboardService.deleteLog(SecurityUtil.getLoginUserNo(), recruitmentNo);
		}
		return ResponseEntity.ok().build();
	}

	@GetMapping("/user/schedule")
	public ResponseEntity<JoinScheduleListResponse> mySchedule() {
		return ResponseEntity.ok(userDtoService.findUserSchedule(SecurityUtil.getLoginUserNo()));
	}

	@GetMapping("/user/recruitment")
	public ResponseEntity<JoinRecruitmentListResponse> myRecruitment() {
		return ResponseEntity.ok(userDtoService.findUserRecruitment(SecurityUtil.getLoginUserNo()));
	}
}
