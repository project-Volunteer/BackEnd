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
import project.volunteer.domain.image.dao.ImageRepository;
import project.volunteer.domain.image.domain.Image;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitmentParticipation.domain.RecruitmentParticipation;
import project.volunteer.global.common.component.Address;
import project.volunteer.global.common.component.Coordinate;
import project.volunteer.domain.recruitmentParticipation.repository.ParticipantRepository;
import project.volunteer.domain.recruitment.application.RecruitmentCommandUseCase;
import project.volunteer.domain.recruitment.application.dto.command.RecruitmentCreateCommand;
import project.volunteer.domain.recruitment.repository.RecruitmentRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.image.domain.Storage;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.domain.Gender;
import project.volunteer.domain.user.domain.Role;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.HourFormat;
import project.volunteer.global.common.component.ParticipantState;
import project.volunteer.global.common.component.Timetable;
import project.volunteer.global.infra.s3.FileService;
import project.volunteer.document.restdocs.config.RestDocsConfiguration;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@AutoConfigureRestDocs
@Import(RestDocsConfiguration.class)
public class UserControllerRecruitingTest {
	@Autowired UserRepository userRepository;
	@Autowired ParticipantRepository participantRepository;
	@Autowired RecruitmentRepository recruitmentRepository;
	@Autowired ImageRepository imageRepository;
	@Autowired
	RecruitmentCommandUseCase recruitmentService;
	@Autowired ImageService imageService;
	@Autowired FileService fileService;
	@PersistenceContext EntityManager em;
	@Autowired MockMvc mockMvc;
	@Autowired RestDocumentationResultHandler restDocs;

	private static User saveUser;
	private List<Long> deleteS3ImageNoList = new ArrayList<>();
	private List<Long> saveRecruitmentNoList = new ArrayList<>();
	final String AUTHORIZATION_HEADER = "accessToken";

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
		// 유저 추가
		String id2 = "kakao_222222";
		String id3 = "kakao_333333";
		String id4 = "kakao_444444";

		String nickName2 = "nickname2";
		String nickName3 = "nickname3";
		String nickName4 = "nickname4";

		String email2 = "email22@gmail.com";
		String email3 = "email33@gmail.com";
		String email4 = "email44@gmail.com";

		String picture2 = "picture2";
		String picture3 = "picture3";
		String picture4 = "picture4";

		String providerId2 = "222222";
		String providerId3 = "333333";
		String providerId4 = "444444";

		User userNo2 = userRepository.save(User.builder().id(id2).password("1234").nickName(nickName2)
				.email(email2).gender(Gender.M).birthDay(LocalDate.now()).picture(picture2)
				.joinAlarmYn(true).beforeAlarmYn(true).noticeAlarmYn(true).role(Role.USER).provider("kakao")
				.providerId(providerId2).build());
        

		User userNo3 = userRepository.save(User.builder().id(id3).password("1234").nickName(nickName3)
				.email(email3).gender(Gender.M).birthDay(LocalDate.now()).picture(picture2)
				.joinAlarmYn(true).beforeAlarmYn(true).noticeAlarmYn(true).role(Role.USER).provider("kakao")
				.providerId(providerId3).build());
        

		User userNo4 = userRepository.save(User.builder().id(id4).password("1234").nickName(nickName4)
				.email(email4).gender(Gender.M).birthDay(LocalDate.now()).picture(picture4)
				.joinAlarmYn(true).beforeAlarmYn(true).noticeAlarmYn(true).role(Role.USER).provider("kakao")
				.providerId(providerId4).build());

		// 모집글 데이터
		VolunteeringCategory category1 = VolunteeringCategory.ADMINSTRATION_ASSISTANCE;
		VolunteeringCategory category2 = VolunteeringCategory.CULTURAL_EVENT;
		VolunteeringType volunteeringType = VolunteeringType.IRREG;
		VolunteerType volunteerType1 = VolunteerType.ALL; // all
		VolunteerType volunteerType2 = VolunteerType.TEENAGER; // teenager
		Boolean isIssued1 = true;
		Boolean isIssued2 = false;
		String sido1 = "11";
		String sido2 = "22";
		String sigungu1 = "1111";
		String sigungu2 = "2222";
		String organizationName = "name";
		String details = "details";
		Float latitude = 3.2F, longitude = 3.2F;
		Integer volunteerNum1 = 10;
		Integer volunteerNum2 = 20;
		String startDay = "01-01-2000";
		String endDay = "01-01-2000";
		String hourFormat = HourFormat.AM.name();
		String startTime = "01:01";
		Integer progressTime = 3;
		String title = "title";
		String content = "content";
		Boolean isPublished = true;

		RecruitmentCreateCommand saveRecruitDto1 = new RecruitmentCreateCommand(title, content, category1,
				volunteeringType, volunteerType1, volunteerNum1, isIssued1, organizationName, isPublished,
				new Address(sido1, sigungu1, details, "fullName"), new Coordinate(latitude, longitude),
				Timetable.of(startDay, endDay, hourFormat, startTime, progressTime),
				null, true, null);

		RecruitmentCreateCommand saveRecruitDto2 = new RecruitmentCreateCommand(title, content, category2,
				volunteeringType, volunteerType2, volunteerNum2, isIssued2, organizationName, isPublished,
				new Address(sido2, sigungu2, details, "fullName"), new Coordinate(latitude, longitude),
				Timetable.of(startDay, endDay, hourFormat, startTime, progressTime),
				null, true, null);
		Long no1 = recruitmentService.addRecruitment(saveUser, saveRecruitDto1);
		Long no2 = recruitmentService.addRecruitment(saveUser, saveRecruitDto2);

		saveRecruitmentNoList.add(no1);
		saveRecruitmentNoList.add(no2);

		// 참여자 저장
		Recruitment recruitment1 = recruitmentRepository.findById(no1).get();
		RecruitmentParticipation participant1 = RecruitmentParticipation.builder().participant(saveUser).recruitment(recruitment1)
				.state(ParticipantState.JOIN_APPROVAL)
				.build();
		participantRepository.save(participant1);

		Recruitment recruitment2 = recruitmentRepository.findById(no2).get();
		RecruitmentParticipation participant2 = RecruitmentParticipation.builder().participant(saveUser).recruitment(recruitment2)
				.state(ParticipantState.JOIN_APPROVAL)
				.build();
		participantRepository.save(participant2);


		Recruitment recruitment3 = recruitmentRepository.findById(no2).get();
		RecruitmentParticipation participant3 = RecruitmentParticipation.builder().participant(userNo2).recruitment(recruitment3)
				.state(ParticipantState.JOIN_APPROVAL)
				.build();
		participantRepository.save(participant3);


		Recruitment recruitment4 = recruitmentRepository.findById(no1).get();
		RecruitmentParticipation participant4 = RecruitmentParticipation.builder().participant(userNo3).recruitment(recruitment4)
				.state(ParticipantState.JOIN_APPROVAL)
				.build();
		participantRepository.save(participant4);


		Recruitment recruitment5 = recruitmentRepository.findById(no1).get();
		RecruitmentParticipation participant5 = RecruitmentParticipation.builder().participant(userNo4).recruitment(recruitment5)
				.state(ParticipantState.JOIN_REQUEST)
				.build();
		participantRepository.save(participant5);
		


		Recruitment recruitment6 = recruitmentRepository.findById(no2).get();
		RecruitmentParticipation participant6 = RecruitmentParticipation.builder().participant(userNo3).recruitment(recruitment6)
				.state(ParticipantState.JOIN_APPROVAL)
				.build();
		participantRepository.save(participant6);
		
		clear();
		
		// init 데이터 요약
		/*
			사용자 4명 생성(init 포함)
			모집글 2개 생성(staticImg, S3업로드Img)
			
			참여자 요약
			사용자 1 : 모집글 1번, 2번 승인(모임장)  
			사용자 2 : 모집글 2번 승인
			사용자 3 : 모집글 1번, 2번 승인
			사용자 4 : 모집글 2번 신청
		 */
	}

	@AfterEach
	public void deleteS3Image() { // S3에 테스트를 위해 저장한 이미지 삭제
		for (Long id : deleteS3ImageNoList) {
			Image image = imageRepository.findById(id).get();
			Storage storage = image.getStorage();
			fileService.deleteFile(storage.getFakeImageName());
		}
	}
	

    @Test
    @WithUserDetails(value = "kakao_111111", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void mypageRecruiting() throws Exception {
        //init
        setData();

		ResultActions result = mockMvc.perform(RestDocumentationRequestBuilders.get("/user/recruiting")
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
										fieldWithPath("recruitingList").type(JsonFieldType.ARRAY).description("모집중인 봉사 모집글 리스트")
								).andWithPrefix("recruitingList.[].",
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
										fieldWithPath("volunteerType").type(JsonFieldType.STRING).description("봉사 모집글의 봉사자 유형 코드 Code VolunteerType 참고바람."),
										fieldWithPath("volunteerNum").type(JsonFieldType.NUMBER).description("봉사 모집글의 봉사 모집 인원수 코드"),
										fieldWithPath("currentVolunteerNum").type(JsonFieldType.NUMBER).description("봉사 모집글의 봉사 참여 인원수")
								)
						)
				);

    }

    @Test
	@Disabled
    @WithUserDetails(value = "kakao_111111", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void 나의_모집글_모집중_리스트조회_null() throws Exception {
        //when & then
        mockMvc.perform(
                get("/user/recruiting"))
                .andExpect(status().isOk())
                .andDo(print());
    }
	
}
