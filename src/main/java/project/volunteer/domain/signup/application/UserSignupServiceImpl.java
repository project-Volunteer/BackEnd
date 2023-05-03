package project.volunteer.domain.signup.application;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import project.volunteer.domain.signup.api.dto.request.UserSignupRequest;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.domain.Role;
import project.volunteer.domain.user.domain.User;

@Service
@Transactional
@RequiredArgsConstructor
public class UserSignupServiceImpl implements UserSignupService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	
	@Override
	public Long addUser(UserSignupRequest userSignupRequest) {
		User user = User.builder()
					.id("kakao_"+userSignupRequest.getProviderId())
					.password(passwordEncoder.encode("kakao"))
					.nickName(userSignupRequest.getNickName())
					.email(userSignupRequest.getEmail())
					.gender(userSignupRequest.getGender())
					.birthDay(userSignupRequest.getBirthday())
					.picture(userSignupRequest.getProfile())
					.role(Role.USER)
					.provider("kakao")
					.providerId(userSignupRequest.getProviderId())
					.noticeAlarmYn(true)
				    .joinAlarmYn(true)
				    .noticeAlarmYn(true)
				    .beforeAlarmYn(true)
					.build();
		
		return userRepository.save(user).getUserNo();
	}

	@Override
	public Boolean checkDuplicatedUser(String id) {
		Optional<User> findUser = userRepository.findById(id);
		return findUser.isPresent();
	}
}
