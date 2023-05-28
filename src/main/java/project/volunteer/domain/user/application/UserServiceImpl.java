package project.volunteer.domain.user.application;

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
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
	private final UserRepository userRepository;

	@Transactional
	@Override
	public void userAlarmUpdate(Long userNo, Boolean joinAlarm, Boolean noticeAlarm, Boolean beforeAlarm) {
		User user = isUserExists(userNo);
		
		user.changeAlarm(joinAlarm, noticeAlarm, beforeAlarm);
		
	}

	@Transactional
	@Override
	public void userInfoUpdate(Long userNo, String nickname, String email, String picture) {
		User user = isUserExists(userNo);
		
		if(picture == null) {
			picture = user.getPicture();
		}
		
		user.changeProfile(nickname, email, picture);
	}
	
	// 유저 존재 유무 확인
	public User isUserExists(Long userNo) {
		return userRepository.findByUserNo(userNo)
				.orElseThrow(()-> new BusinessException(ErrorCode.NOT_EXIST_USER, 
						String.format("not found user = [%d]", userNo)));
	}
}
