package project.volunteer.domain.user.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.time.LocalDate;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.databind.ObjectMapper;

import project.volunteer.domain.image.dao.ImageRepository;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.domain.Gender;
import project.volunteer.domain.user.domain.Role;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.infra.s3.FileService;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class UserControllerInfoUpdateTest {
	@Autowired ObjectMapper objectMapper;
	@Autowired UserRepository userRepository;
	@Autowired MockMvc mockMvc;
	@Autowired ImageRepository imageRepository;
	@Autowired FileService fileService;
	@PersistenceContext EntityManager em;

	private static User saveUser;
	
	private MockMultipartFile getFakeMockMultipartFile() throws IOException {
		return new MockMultipartFile(
				"picture.uploadImage", "".getBytes());
	}
    

	private void clear() {
		em.flush();
		em.clear();
	}


	String changedNickName= "changedNickName";
	String changedEmail= "changedEmail@test.test";
	
	@BeforeEach
	public void initUser() {
		saveUser = userRepository.save(User.builder()
				.id("kakao_111111")
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
	@WithUserDetails(value = "kakao_111111", setupBefore = TestExecutionEvent.TEST_EXECUTION)
	void 프로필_수정_프로필사진변경() throws Exception {
        //given
        MultiValueMap param = new LinkedMultiValueMap<>();
        param.add("nickName", changedNickName);
        param.add("email", changedEmail);

        //when & then
        mockMvc.perform(
                multipart("/user")
                        .file(getFakeMockMultipartFile())
                        .params(param)
                )
                .andExpect(status().isOk())
                .andDo(print());
	}
	
	@Test
	@WithUserDetails(value = "kakao_111111", setupBefore = TestExecutionEvent.TEST_EXECUTION)
	void 프로필_수정_프로필사진변경X() throws Exception {
        //given
        MultiValueMap param = new LinkedMultiValueMap<>();
        param.add("nickName", changedNickName);
        param.add("email", changedEmail);

        //when & then
        mockMvc.perform(
                multipart("/user")
                        .params(param)
                )
                .andExpect(status().isOk())
                .andDo(print());
	}
	
	@Test
	@WithUserDetails(value = "kakao_111111", setupBefore = TestExecutionEvent.TEST_EXECUTION)
	void 프로필_수정_필수값체크() throws Exception {
        //given
        MultiValueMap param = new LinkedMultiValueMap<>();

        //when & then
        mockMvc.perform(
                multipart("/user")
                        .params(param)
                )
                .andExpect(status().isBadRequest())
                .andDo(print());
	}
}
