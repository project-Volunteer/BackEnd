package project.volunteer.domain.user.api;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.databind.ObjectMapper;

import project.volunteer.domain.image.dao.ImageRepository;
import project.volunteer.domain.image.dao.StorageRepository;
import project.volunteer.domain.image.domain.Storage;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.domain.Gender;
import project.volunteer.domain.user.domain.Role;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.infra.s3.FileService;
import project.volunteer.restdocs.document.config.RestDocsConfiguration;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@AutoConfigureRestDocs
@Import(RestDocsConfiguration.class)
public class UserControllerInfoUpdateTest {
	@Autowired ObjectMapper objectMapper;
	@Autowired UserRepository userRepository;
	@Autowired MockMvc mockMvc;
	@Autowired ImageRepository imageRepository;
	@Autowired FileService fileService;
	@Autowired RestDocumentationResultHandler restDocs;
	@Autowired private StorageRepository storageRepository;
	@PersistenceContext EntityManager em;

	private static User saveUser;
	final String AUTHORIZATION_HEADER = "accessToken";


	private MockMultipartFile getRealMockMultipartFile() throws IOException {
		return new MockMultipartFile(
				"profile", "file.PNG", "image/jpg", new FileInputStream("src/main/resources/static/test/file.PNG"));
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
	void profileModify() throws Exception {
        //given
        MultiValueMap param = new LinkedMultiValueMap<>();
        param.add("nickName", changedNickName);
        param.add("email", changedEmail);

        //when & then
		ResultActions result = mockMvc.perform(
                multipart("/user")
                        .file(getRealMockMultipartFile())
						.header(AUTHORIZATION_HEADER, "access Token")
                        .params(param)
                );

                result.andExpect(status().isOk())
                .andDo(print())
				.andDo(
						restDocs.document(
								requestHeaders(
										headerWithName(AUTHORIZATION_HEADER).description("JWT Access Token")
								),
								requestParts(
										partWithName("profile").description("변경할 프로필 이미지")
								),
								requestParameters(
										parameterWithName("nickName").description("변경할 닉네임"),
										parameterWithName("email").description("변경할 이메일")
								)
						)
				);
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

	@AfterEach
	void deleteUploadImage(){
		List<Storage> storages = storageRepository.findAll();
		storages.stream().forEach(s -> fileService.deleteFile(s.getFakeImageName()));
	}

}
