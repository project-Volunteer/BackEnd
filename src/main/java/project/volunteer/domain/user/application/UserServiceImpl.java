package project.volunteer.domain.user.application;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.volunteer.domain.image.application.ImageService;
import project.volunteer.domain.image.application.dto.ImageParam;
import project.volunteer.domain.image.dao.ImageRepository;
import project.volunteer.domain.image.domain.Image;
import project.volunteer.domain.image.domain.ImageType;
import project.volunteer.domain.image.domain.RealWorkCode;
import project.volunteer.domain.user.api.dto.response.UserAlarmResponse;
import project.volunteer.domain.user.api.dto.response.UserJoinRequestListResponse;
import project.volunteer.domain.user.api.dto.response.UserRecruitingListResponse;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.dao.queryDto.UserQueryDtoRepository;
import project.volunteer.domain.user.dao.queryDto.dto.UserRecruitingQuery;
import project.volunteer.domain.user.dao.queryDto.dto.UserRecruitmentJoinRequestQuery;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;
import project.volunteer.global.util.SecurityUtil;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
	
	private final UserRepository userRepository;
	private final UserQueryDtoRepository userQueryDtoRepository;
    private final ImageRepository imageRepository;
	private final ImageService imageService;
	
	@Override
	public UserJoinRequestListResponse findUserJoinRequest(Long userNo) {
		User user = userRepository.findByUserNo(userNo)
				.orElseThrow(()-> new BusinessException(ErrorCode.NOT_EXIST_USER, 
						String.format("not found user = [%d]", SecurityUtil.getLoginUserId())));
		
		List<UserRecruitmentJoinRequestQuery> data = userQueryDtoRepository.findUserRecruitmentJoinRequestDto(userNo);
		
		return new UserJoinRequestListResponse(data);
	}

	@Override
	public UserRecruitingListResponse findUserRecruiting(Long userNo) {
		User user = userRepository.findByUserNo(userNo)
				.orElseThrow(()-> new BusinessException(ErrorCode.NOT_EXIST_USER, 
						String.format("not found user = [%d]", SecurityUtil.getLoginUserId())));
		
		List<UserRecruitingQuery> data = userQueryDtoRepository.findUserRecruitingDto(userNo);
		
		return new UserRecruitingListResponse(data);
	}

	@Override
	public UserAlarmResponse findUserAlarm(Long userNo) {
		User user = userRepository.findByUserNo(userNo)
				.orElseThrow(()-> new BusinessException(ErrorCode.NOT_EXIST_USER, 
						String.format("not found user = [%d]", SecurityUtil.getLoginUserId())));
		
		return new UserAlarmResponse(user.getJoinAlarmYn(), user.getNoticeAlarmYn(), user.getBeforeAlarmYn());
	}

	@Transactional
	@Override
	public void userAlarmUpdate(Long userNo, Boolean joinAlarm, Boolean noticeAlarm, Boolean beforeAlarm) {
		User user = userRepository.findByUserNo(userNo)
				.orElseThrow(()-> new BusinessException(ErrorCode.NOT_EXIST_USER, 
						String.format("not found user = [%d]", SecurityUtil.getLoginUserId())));
		
		user.setJoinAlarmYn(joinAlarm);
		user.setNoticeAlarmYn(noticeAlarm);
		user.setBeforeAlarmYn(beforeAlarm);
		
	}

	@Transactional
	@Override
	public void userInfoUpdate(Long userNo, String nickname, String email, MultipartFile profile) {
		User user = userRepository.findByUserNo(userNo)
				.orElseThrow(()-> new BusinessException(ErrorCode.NOT_EXIST_USER, 
						String.format("not found user = [%d]", SecurityUtil.getLoginUserId())));
		
		user.setNickName(nickname);
		user.setEmail(email);
		
		if(profile != null) {
			imageService.deleteImage(RealWorkCode.USER, userNo);
			ImageParam uploadUserProfile = new ImageParam(RealWorkCode.USER, userNo, ImageType.UPLOAD, null, profile);
			imageService.addImage(uploadUserProfile);
			Optional<Image> savedImg = imageRepository.findEGStorageByCodeAndNo(RealWorkCode.USER, userNo);
			
			user.setPicture(savedImg.get().getStorage().getImagePath());
		}
	}
}
