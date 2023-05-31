package project.volunteer.domain.signup.application;

import java.io.IOException;
import java.time.LocalDate;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import project.volunteer.domain.signup.api.dto.request.UserSignupRequest;
import project.volunteer.domain.signup.application.UserSignupService;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.domain.Gender;
import project.volunteer.domain.user.domain.Role;
import project.volunteer.domain.user.domain.User;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserSignupServiceImplTest {
	@Autowired UserSignupService userSignupService;
	@Autowired UserRepository userRepository;
	@PersistenceContext EntityManager em;
	@Autowired MockMvc mockMvc;

	private void clear() {
		em.flush();
		em.clear();
	}
	
	private void setData() throws IOException {
		userRepository.save(User.builder()
				.id("kakao_123456789")
				.password("1234")
				.nickName("nickname11")
				.email("email11@gmail.com")
				.gender(Gender.M)
				.birthDay(LocalDate.now())
				.picture("picture")
				.joinAlarmYn(true).beforeAlarmYn(true).noticeAlarmYn(true)
				.role(Role.USER)
				.provider("kakao")
				.providerId("111111")
				.build());
		clear();
	}
	
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

	
	@Test
	void 중복회원가입() throws Exception {
		// given
		setData();
		
		// when
		Boolean duplicated = userSignupService.checkDuplicatedUser("kakao_123456789");
		
		// then
		Assertions.assertThat(duplicated).isTrue();
	}

}
