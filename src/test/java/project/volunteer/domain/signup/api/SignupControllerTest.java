package project.volunteer.domain.signup.api;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import project.volunteer.domain.signup.api.dto.request.UserSignupRequest;
import project.volunteer.domain.user.domain.Gender;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SignupControllerTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@Test
	public void 메일발송_이메일_형식이_아님() throws Exception {
		String requestJson = "{\"email\":\"jw_passion\"}";

		mockMvc.perform(post("/signup/email").contentType(MediaType.APPLICATION_JSON).content(requestJson))
				.andExpect(jsonPath("resultMessage", is("Failed to send mail"))).andDo(MockMvcResultHandlers.print());
	}

	@Test
	public void 메일발송_성공() throws Exception {
		String requestJson = "{\"email\":\"jw_passion@naver.com\"}";

		mockMvc.perform(post("/signup/email").contentType(MediaType.APPLICATION_JSON).content(requestJson))
				.andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print());
	}

	@Test
	public void 회원가입_vaildationcheck() throws Exception {
		String requestJson = "{\"providerId\": \"123456\","
				+ "\"nickName\": \"nickName\","
				+ "\"profile\": \"profile\","
				+ "\"email\": \"email\","
				+ "\"birthday\": \"2000-11-1\","
				+ "\"gender\": 1}";
		
		System.out.println(Gender.M.getCode());
		
		MockHttpServletRequestBuilder builder = 
				post("/signup/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson);
		
        MvcResult result = mockMvc.perform(builder).andReturn();
		
		String message = result.getResolvedException().getMessage();
		System.out.println("message = " + message);
		
		Assertions.assertThat(HttpStatus.BAD_REQUEST);
		// 필수 입력 값입니다.
		// 이메일 형식에 맞지 않습니다.
		// 날짜 포맷이 맞지 않습니다.
		Assertions.assertThat(message).contains("이메일 형식에 맞지 않습니다.","날짜 포맷이 맞지 않습니다.");
		
	}

	@Test
	public void 회원가입_성공() throws Exception {
		UserSignupRequest userSignupDTO = new UserSignupRequest();
		userSignupDTO.setProviderId("123456");
		userSignupDTO.setNickName("nickName");
		userSignupDTO.setProfile("profile");
		userSignupDTO.setEmail("email@naver.com");
		userSignupDTO.setBirthday("2000-11-22");
		userSignupDTO.setGender(1);

		mockMvc.perform(post("/signup/user").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(userSignupDTO)))
				.andExpect(MockMvcResultMatchers.status().isOk()).andDo(MockMvcResultHandlers.print());
	}
}
