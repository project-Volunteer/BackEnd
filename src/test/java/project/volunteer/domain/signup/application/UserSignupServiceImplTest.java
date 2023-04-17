package project.volunteer.domain.signup.application;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import project.volunteer.domain.signup.api.dto.request.UserSignupRequest;
import project.volunteer.domain.signup.application.UserSignupService;
import project.volunteer.domain.user.domain.Gender;
import project.volunteer.domain.user.domain.User;

@SpringBootTest
class UserSignupServiceImplTest {
	@Autowired
	UserSignupService userSignupService;
	
	@Test
	void 회원가입_성공() {
		// given
		UserSignupRequest userSignupDTO = new UserSignupRequest();
		userSignupDTO.setGender(1);
		userSignupDTO.setBirthday("2000-11-11");
		userSignupDTO.setNickName("nickName");
		userSignupDTO.setEmail("jw_passion@naver.com");
		userSignupDTO.setProfile("profile");
		userSignupDTO.setProviderId("123456789");
		
		// when
		userSignupService.addUser(userSignupDTO);
		
		// then
		
	}

}
