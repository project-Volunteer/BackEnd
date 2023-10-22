package project.volunteer.domain.user.api;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import project.volunteer.domain.image.application.ImageService;
import project.volunteer.domain.image.application.dto.ImageParam;
import project.volunteer.domain.image.dao.ImageRepository;
import project.volunteer.domain.image.dao.StorageRepository;
import project.volunteer.global.common.component.RealWorkCode;
import project.volunteer.domain.participation.dao.ParticipantRepository;
import project.volunteer.domain.participation.domain.Participant;
import project.volunteer.domain.recruitment.application.RecruitmentService;
import project.volunteer.domain.recruitment.application.dto.RecruitmentParam;
import project.volunteer.domain.recruitment.dao.RecruitmentRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.image.domain.Storage;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.domain.Gender;
import project.volunteer.domain.user.domain.Role;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.HourFormat;
import project.volunteer.global.common.component.ParticipantState;
import project.volunteer.global.infra.s3.FileService;
import project.volunteer.restdocs.document.config.RestDocsConfiguration;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@AutoConfigureRestDocs
@Import(RestDocsConfiguration.class)
public class UserControllerJoinRequestTest {
	@Autowired UserRepository userRepository;
	@Autowired ParticipantRepository participantRepository;
	@Autowired RecruitmentRepository recruitmentRepository;
	@Autowired ImageRepository imageRepository;
	@Autowired StorageRepository storageRepository;
	@Autowired RecruitmentService recruitmentService;
	@Autowired ImageService imageService;
	@Autowired FileService fileService;
	@PersistenceContext EntityManager em;
	@Autowired MockMvc mockMvc;
	@Autowired RestDocumentationResultHandler restDocs;
	final String AUTHORIZATION_HEADER = "accessToken";

	private static User saveUser;
	private List<Long> deleteS3ImageNoList = new ArrayList<>();
	private List<Long> saveRecruitmentNoList = new ArrayList<>();

    private MockMultipartFile getMockMultipartFile() throws IOException {
        return new MockMultipartFile(
                "file", "file.PNG", "image/jpg", new FileInputStream("src/main/resources/static/test/file.PNG"));
    }
    
	private void clear() {
		em.flush();
		em.clear();
	}

    @BeforeEach
    public void initUser(){
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
    
	private void setData() throws IOException {
		// 모집글 데이터
		String category1 = "001";
		String category2 = "002";
		String volunteeringType = VolunteeringType.IRREG.name();
		String volunteerType1 = "1"; // all
		String volunteerType2 = "3"; // teenager
		Boolean isIssued1 = true;
		Boolean isIssued2 = false;
		String sido1 = "11";
		String sido2 = "22";
		String sigungu1 = "1111";
		String sigungu2 = "2222";
		String organizationName = "name";
		String details = "details";
		Float latitude = 3.2F, longitude = 3.2F;
		Integer volunteerNum = 5;
		String startDay = "01-01-2000";
		String endDay = "01-01-2000";
		String hourFormat = HourFormat.AM.name();
		String startTime = "01:01";
		Integer progressTime = 3;
		String title = "title", content = "content";
		Boolean isPublished = true;

		RecruitmentParam saveRecruitDto1 = new RecruitmentParam(category1, organizationName, sido1, sigungu1,
				details, "fullName", latitude, longitude, isIssued1, volunteerType1, volunteerNum, volunteeringType, startDay,
				endDay, hourFormat, startTime, progressTime, title, content, isPublished);
		RecruitmentParam saveRecruitDto2 = new RecruitmentParam(category2, organizationName, sido2, sigungu2,
				details, "fullName", latitude, longitude, isIssued2, volunteerType2, volunteerNum, volunteeringType, startDay,
				endDay, hourFormat, startTime, progressTime, title, content, isPublished);
		Long no1 = recruitmentService.addRecruitment(saveUser, saveRecruitDto1).getRecruitmentNo();
		Long no2 = recruitmentService.addRecruitment(saveUser, saveRecruitDto2).getRecruitmentNo();

		saveRecruitmentNoList.add(no1);
		saveRecruitmentNoList.add(no2);

		ImageParam uploadImageDto = ImageParam.builder().code(RealWorkCode.RECRUITMENT).no(no2).uploadImage(getMockMultipartFile()).build();
		Long saveId2 = imageService.addImage(uploadImageDto);
		deleteS3ImageNoList.add(saveId2); // S3에 저장된 이미지 추후 삭제 예정
	
	
		// 참여자 저장
		Recruitment recruitment1 = recruitmentRepository.findById(no1).get();
		Participant participant1 = Participant.builder().participant(saveUser).recruitment(recruitment1)
				.state(ParticipantState.JOIN_REQUEST)
				.build();
		participantRepository.save(participant1);

		Recruitment recruitment2 = recruitmentRepository.findById(no2).get();
		Participant participant2 = Participant.builder().participant(saveUser).recruitment(recruitment2)
				.state(ParticipantState.JOIN_REQUEST)
				.build();
		participantRepository.save(participant2);
		
		clear();

		// init 데이터 요약
		/*
			사용자 1명 생성
			모집글 2개 생성(staticImg, S3업로드Img)
			참여 사용자 1 : 모집글 1번, 2번 신청
		 */
		
	}

	@AfterEach
	public void deleteS3Image() {
		List<Storage> storages = storageRepository.findAll();
		storages.stream().forEach(s -> fileService.deleteFile(s.getFakeImageName()));
	}
	

    @Test
    @WithUserDetails(value = "kakao_111111", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void mypageJoinRequest() throws Exception {
        //init
        setData();
        
        //when & then
		ResultActions result = mockMvc.perform(RestDocumentationRequestBuilders.get("/user/request")
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
										fieldWithPath("requestList").type(JsonFieldType.ARRAY).description("승인 대기중인 봉사 모집글 리스트")
								).andWithPrefix("requestList.[].",
										fieldWithPath("no").type(JsonFieldType.NUMBER).description("봉사 모집글 고유키 PK"),
										fieldWithPath("picture.isStaticImage").type(JsonFieldType.BOOLEAN).description("봉사모집글의 정적/동적 이미지 구분"),
										fieldWithPath("picture.uploadImage").type(JsonFieldType.STRING).optional().description("봉사 모집글의 업로드 이미지 URL, isStaticImage True 일 경우 NULL"),
										fieldWithPath("startDay").type(JsonFieldType.STRING).description("봉사 모집글의 시작일자"),
										fieldWithPath("endDay").type(JsonFieldType.STRING).description("봉사 모집글의 종료일자"),
										fieldWithPath("title").type(JsonFieldType.STRING).description("봉사 모집글의 제목"),
										fieldWithPath("sido").type(JsonFieldType.STRING).description("봉사 모집글의 시/구 코드"),
										fieldWithPath("sigungu").type(JsonFieldType.STRING).description("봉사 모집글의 시/군/구 코드"),
										fieldWithPath("volunteeringCategory").type(JsonFieldType.STRING).description("봉사 모집글의 봉사카테고리 코드 Code VolunteeringCategory 참고바람"),
										fieldWithPath("volunteeringType").type(JsonFieldType.STRING).description("봉사 모집글의 봉사 유형 코드 Code VolunteeringType 참고바람"),
										fieldWithPath("isIssued").type(JsonFieldType.BOOLEAN).description("봉사 모집글의 봉사 시간 인증 가능 여부"),
										fieldWithPath("volunteerType").type(JsonFieldType.STRING).description("봉사 모집글의 봉사자 유형 코드 Code VolunteerType 참고바람.")
								)
						)
				);
    }

    @Test
	@Disabled
    @WithUserDetails(value = "kakao_111111", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void 나의_모집글_승인대기_리스트조회_null() throws Exception {
        //when & then
        mockMvc.perform(
                get("/user/request"))
                .andExpect(status().isOk())
                .andDo(print());
    }
	
}
