package project.volunteer.domain.signup.application;

import java.time.LocalDate;

import org.assertj.core.api.Assertions;
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
		userSignupDTO.setGender(Gender.M);
		userSignupDTO.setBirthday(LocalDate.parse("2000-11-22"));
		userSignupDTO.setNickName("nickName");
		userSignupDTO.setEmail("jw_passion@naver.com");
		userSignupDTO.setProfile("profile");
		userSignupDTO.setProviderId("123456789");
		
		// when
		Long userNo = userSignupService.addUser(userSignupDTO);
		
		// then
		System.out.println("userNo = " + userNo);
		Assertions.assertThat(userNo).isNotNull();
		
	}

}
