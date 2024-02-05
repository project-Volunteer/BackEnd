package project.volunteer.domain.user.api;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import project.volunteer.domain.user.api.dto.request.UserAlarmRequestParam;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.domain.Gender;
import project.volunteer.domain.user.domain.Role;
import project.volunteer.domain.user.domain.User;
import project.volunteer.document.restdocs.config.RestDocsConfiguration;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@AutoConfigureRestDocs
@Import(RestDocsConfiguration.class)
public class UserControllerAlarmTest {
	@Autowired ObjectMapper objectMapper;
	@Autowired UserRepository userRepository;
	@Autowired MockMvc mockMvc;
	@PersistenceContext EntityManager em;
	@Autowired RestDocumentationResultHandler restDocs;

	final String AUTHORIZATION_HEADER = "accessToken";

	private static User saveUser;

	private void clear() {
		em.flush();
		em.clear();
	}

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

    private <T> String toJson(T data) throws JsonProcessingException {
        return objectMapper.writeValueAsString(data);
    }

	@Test
	@WithUserDetails(value = "kakao_111111", setupBefore = TestExecutionEvent.TEST_EXECUTION)
	void mypageAlarm() throws Exception {
		ResultActions result = mockMvc.perform(RestDocumentationRequestBuilders.get("/user/alarm")
				.header(AUTHORIZATION_HEADER, "access Token")
		);

		//then
		result.andExpect(status().isOk())
				.andDo(print())
				.andDo(
						restDocs.document(
								requestHeaders(
										headerWithName(AUTHORIZATION_HEADER).description("JWT Access Token")
								),
								responseFields(
										fieldWithPath("joinAlarm").type(JsonFieldType.BOOLEAN).description("참가자 알림"),
										fieldWithPath("noticeAlarm").type(JsonFieldType.BOOLEAN).description("공지 사항 게시물 알림"),
										fieldWithPath("beforeAlarm").type(JsonFieldType.BOOLEAN).description("봉사활동 전날 알림")
								)
						)
				);
	}

	@Test
	@WithUserDetails(value = "kakao_111111", setupBefore = TestExecutionEvent.TEST_EXECUTION)
	void mypageAlarmModify() throws Exception {
		UserAlarmRequestParam param = new UserAlarmRequestParam(Boolean.FALSE, Boolean.FALSE, Boolean.FALSE);
		// when & then

		//when
		ResultActions result = mockMvc.perform(RestDocumentationRequestBuilders.put("/user/alarm")
				.contentType(MediaType.APPLICATION_JSON)
				.header(AUTHORIZATION_HEADER, "access Token")
				.content(toJson(param))
		);

		//then
		result.andExpect(status().isOk())
				.andDo(print())
				.andDo(
						restDocs.document(
								requestHeaders(
										headerWithName(AUTHORIZATION_HEADER).description("JWT Access Token")
								),
								requestFields(
										fieldWithPath("joinAlarm").type(JsonFieldType.BOOLEAN).description("참가자 알림"),
										fieldWithPath("noticeAlarm").type(JsonFieldType.BOOLEAN).description("공지 사항 게시물 알림"),
										fieldWithPath("beforeAlarm").type(JsonFieldType.BOOLEAN).description("봉사활동 전날 알림")
								)
						)
				);
	}

	@Test
	@Disabled
	@WithUserDetails(value = "kakao_111111", setupBefore = TestExecutionEvent.TEST_EXECUTION)
	void 나의_알람_수정_null() throws Exception {
		UserAlarmRequestParam param = new UserAlarmRequestParam(Boolean.FALSE, Boolean.FALSE, null);
		// when & then
		mockMvc.perform(
				put("/user/alarm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(param)))
		        .andExpect(status().is4xxClientError())
		        .andDo(print());
	}

}
