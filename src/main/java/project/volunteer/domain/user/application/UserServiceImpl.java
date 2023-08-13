package project.volunteer.domain.user.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.volunteer.domain.user.api.dto.response.UserAlarmResponse;
import project.volunteer.domain.user.api.dto.response.UserInfo;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.validate.UserValidate;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
	private final UserValidate userValidate;

	@Transactional
	@Override
	public void userRefreshTokenUpdate(Long userNo, String refreshToken) {
		User user = userValidate.validateAndGetUser(userNo);
		user.setRefreshToken(refreshToken);
	}

	@Transactional
	@Override
	public void userAlarmUpdate(Long userNo, Boolean joinAlarm, Boolean noticeAlarm, Boolean beforeAlarm) {
		User user = userValidate.validateAndGetUser(userNo);
		user.changeAlarm(joinAlarm, noticeAlarm, beforeAlarm);
	}

	@Transactional
	@Override
	public void userInfoUpdate(Long userNo, String nickname, String email, String picture) {
		User user = userValidate.validateAndGetUser(userNo);
		
		if(picture == null) {
			picture = user.getPicture();
		}
		
		user.changeProfile(nickname, email, picture);
	}

	@Override
	public UserAlarmResponse findUserAlarm(Long userNo) {
		User user = userValidate.validateAndGetUser(userNo);
		return new UserAlarmResponse(user.getJoinAlarmYn(), user.getNoticeAlarmYn(), user.getBeforeAlarmYn());
	}

	@Override
	public UserInfo findUserInfo(Long loginUserNo) {
		User user = userValidate.validateAndGetUser(loginUserNo);
		return new UserInfo(user.getNickName(), user.getEmail(), user.getPicture());
	}

	@Override
	public User findUser(Long userNo) {
		return userValidate.validateAndGetUser(userNo);
	}
}
