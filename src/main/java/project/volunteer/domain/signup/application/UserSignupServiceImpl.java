package project.volunteer.domain.signup.application;

import java.time.LocalDate;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import project.volunteer.domain.signup.api.dto.request.UserSignupRequest;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.domain.Gender;
import project.volunteer.domain.user.domain.User;

@Service
@Transactional
@RequiredArgsConstructor
public class UserSignupServiceImpl implements UserSignupService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	
	@Override
	public void addUser(UserSignupRequest userSignupDTO) {
		Gender gender = (userSignupDTO.getGender() > 0) ? Gender.M : Gender.W;
		LocalDate birthday = LocalDate.parse(userSignupDTO.getBirthday());
		User user = User.builder()
					.nickName(userSignupDTO.getNickName())
					.email(userSignupDTO.getEmail())
					.gender(gender)
					.birthDay(birthday)
					.picture(userSignupDTO.getProfile())
					.provider(passwordEncoder.encode("kakao"))
					.providerId(userSignupDTO.getProviderId())
					.noticeAlarmYn(true)
				    .joinAlarmYn(true)
				    .noticeAlarmYn(true)
				    .beforeAlarmYn(true)
					.build();
		
		userRepository.save(user);
	}

}
