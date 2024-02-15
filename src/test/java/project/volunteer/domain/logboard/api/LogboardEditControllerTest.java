package project.volunteer.domain.logboard.api;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
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
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import project.volunteer.domain.image.application.ImageService;
import project.volunteer.domain.image.application.dto.ImageParam;
import project.volunteer.domain.image.dao.ImageRepository;
import project.volunteer.domain.image.dao.StorageRepository;
import project.volunteer.domain.logboard.dao.LogboardRepository;
import project.volunteer.domain.logboard.domain.Logboard;
import project.volunteer.domain.recruitmentParticipation.repository.ParticipantRepository;
import project.volunteer.domain.recruitmentParticipation.domain.Participant;
import project.volunteer.domain.recruitment.application.RecruitmentCommandUseCase;
import project.volunteer.domain.recruitment.repository.RecruitmentRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.scheduleParticipation.repository.ScheduleParticipationRepository;
import project.volunteer.domain.scheduleParticipation.domain.ScheduleParticipation;
import project.volunteer.domain.sehedule.repository.ScheduleRepository;
import project.volunteer.domain.sehedule.domain.Schedule;
import project.volunteer.domain.image.domain.Storage;
import project.volunteer.domain.user.application.UserDtoService;
import project.volunteer.domain.user.application.UserService;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.domain.Gender;
import project.volunteer.domain.user.domain.Role;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.Address;
import project.volunteer.global.common.component.Coordinate;
import project.volunteer.global.common.component.HourFormat;
import project.volunteer.global.common.component.IsDeleted;
import project.volunteer.global.common.component.ParticipantState;
import project.volunteer.global.common.component.RealWorkCode;
import project.volunteer.global.common.component.Timetable;
import project.volunteer.global.infra.s3.FileService;
import project.volunteer.document.restdocs.config.RestDocsConfiguration;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@AutoConfigureRestDocs
@Import(RestDocsConfiguration.class)
public class LogboardEditControllerTest {
    @Autowired MockMvc mockMvc;
	@Autowired UserRepository userRepository;
	@Autowired ParticipantRepository participantRepository;
	@Autowired RecruitmentRepository recruitmentRepository;
	@Autowired ImageRepository imageRepository;
	@Autowired
	RecruitmentCommandUseCase recruitmentService;
	@Autowired ImageService imageService;
	@Autowired FileService fileService;
	@Autowired UserService userService;
	@Autowired ScheduleRepository scheduleRepository;
	@Autowired UserDtoService userDtoService;
    @Autowired ScheduleParticipationRepository scheduleParticipationRepository;
	@Autowired LogboardRepository logboardRepository;
	@Autowired StorageRepository storageRepository;
	@Autowired RestDocumentationResultHandler restDocs;
	@PersistenceContext EntityManager em;

	final String AUTHORIZATION_HEADER = "accessToken";
	List<Logboard> logboardList= new ArrayList<>();
	
	private static User saveUser;
	private static Schedule createSchedule;
	private static Participant createParticipant;
	private List<Long> deleteS3ImageNoList = new ArrayList<>();
	
    private MockMultipartFile getMockMultipartFile() throws IOException {
        return new MockMultipartFile(
                "picture", "file.PNG", "image/jpg", new FileInputStream("src/main/resources/static/test/file.PNG"));
    }
	
	private MockMultipartFile getFakeMockMultipartFile() throws IOException {
		return new MockMultipartFile(
				"picture", "".getBytes());
	}
    
	private void clear() {
		em.flush();
		em.clear();
	}


    @BeforeEach
    public void initData() throws Exception{
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

        String title = "title";
        String content = "content";
        String organizationName = "organization";
        Timetable timetable = new Timetable(LocalDate.now(), LocalDate.now(), HourFormat.AM, LocalTime.now(), 10);
        Boolean isPublished = true;
        Coordinate coordinate = new Coordinate(3.2F, 3.2F);        
        VolunteeringCategory category = VolunteeringCategory.ADMINSTRATION_ASSISTANCE;
        VolunteeringType volunteeringType = VolunteeringType.IRREG;
        VolunteerType volunteerType = VolunteerType.ALL;
        Boolean isIssued = true;
        String details = "details";
        Address address = new Address("11", "110011", details, "fullName");
        int volunteerNum = 10;

		Recruitment create = Recruitment.builder()
				.title(title).content(content).volunteeringCategory(category).volunteeringType(volunteeringType)
				.volunteerType(volunteerType).maxParticipationNum(volunteerNum).currentVolunteerNum(0).isIssued(isIssued).organizationName(organizationName)
				.address(address).coordinate(coordinate).timetable(timetable).isPublished(isPublished).viewCount(0).likeCount(0)
				.isDeleted(IsDeleted.N).writer(saveUser)
				.build();
		recruitmentRepository.save(create);
		Long no = create.getRecruitmentNo();

		// 방장 참여자 저장
		Recruitment recruitment = recruitmentRepository.findById(no).get();
		createParticipant = Participant.createParticipant(recruitment, saveUser, ParticipantState.JOIN_APPROVAL);
		participantRepository.save(createParticipant);
		

		// 스케줄 저장
		createSchedule = Schedule.create(recruitment, timetable, content, organizationName, address, volunteerNum);
		scheduleRepository.save(createSchedule);
		
		clear();

		// init 데이터 요약
		/*
			사용자 2명 생성
			모집글 1개 생성(staticImg)
			스케쥴 1개 생성
			
			참여자 요약
			사용자 1 : 모집글 승인(모임장)
		 */
    }

	@Test
	@WithUserDetails(value = "kakao_111111", setupBefore = TestExecutionEvent.TEST_EXECUTION)
	public void logboardEdit() throws Exception {
		//given
		일정_참여상태_추가(createParticipant, ParticipantState.PARTICIPATION_COMPLETE_APPROVAL);
		Logboard createLogboard = 로그보드_추가(saveUser);

		MultiValueMap<String, String> info  = new LinkedMultiValueMap<>();
		info.add("content", "logboard test content");
		info.add("scheduleNo", String.valueOf(createSchedule.getScheduleNo()));
		info.add("isPublished", String.valueOf(true));

		//when & then
		ResultActions result = mockMvc.perform(
				RestDocumentationRequestBuilders.multipart("/logboard/edit/{no}",createLogboard.getLogboardNo())
						.file(getMockMultipartFile())
						.file(getMockMultipartFile())
						.file(getMockMultipartFile())
						.header(AUTHORIZATION_HEADER, "access Token")
						.params(info)
		);

		result.andExpect(status().isOk())
				.andDo(print())
				.andDo(
						restDocs.document(
								requestHeaders(
										headerWithName(AUTHORIZATION_HEADER).description("JWT Access Token")
								),
								requestParts(
										partWithName("picture").description("수정할 봉사 로그 이미지")
								),
								requestParameters(
										parameterWithName("content").description("봉사 로그 내용"),
										parameterWithName("scheduleNo").description("봉사 참여 고유키 PK"),
										parameterWithName("isPublished").description("봉사 로그 발행 여부")
								),
								pathParameters(
										parameterWithName("no").description("봉사 로그 고유키 PK")
								)
						)
				);
	}

    
	@Test
	@Disabled
	@WithUserDetails(value = "kakao_111111", setupBefore = TestExecutionEvent.TEST_EXECUTION)
	public void 수정_validation체크_내용_누락() throws Exception {
		//given
		일정_참여상태_추가(createParticipant, ParticipantState.PARTICIPATION_COMPLETE_APPROVAL);
		Logboard createLogboard = 로그보드_추가(saveUser);
		
		MultiValueMap<String, String> info  = new LinkedMultiValueMap<>();
		info.add("content", ""); // 내용 누락
		info.add("scheduleNo", String.valueOf(createSchedule.getScheduleNo()));
		info.add("isPublished", String.valueOf(true));

		//when & then
		mockMvc.perform(
				multipart("/logboard/edit/"+createLogboard.getLogboardNo())
				.file(getFakeMockMultipartFile())
				.file(getFakeMockMultipartFile())
				.file(getFakeMockMultipartFile())
				.params(info)
			)
		.andExpect(status().isBadRequest())
		.andDo(print());
	}

	@Test
	@Disabled
	@WithUserDetails(value = "kakao_111111", setupBefore = TestExecutionEvent.TEST_EXECUTION)
	public void 수정_validation체크_스케줄번호_누락() throws Exception {
		//given
		일정_참여상태_추가(createParticipant, ParticipantState.PARTICIPATION_COMPLETE_APPROVAL);
		Logboard createLogboard = 로그보드_추가(saveUser);
		
		MultiValueMap<String, String> info  = new LinkedMultiValueMap<>();
		info.add("content", "logboard test content"); 
		info.add("scheduleNo", "");// 스케줄번호 누락
		info.add("isPublished", String.valueOf(true));

		//when & then
		mockMvc.perform(
				multipart("/logboard/edit/"+createLogboard.getLogboardNo())
				.file(getFakeMockMultipartFile())
				.file(getFakeMockMultipartFile())
				.file(getFakeMockMultipartFile())
				.params(info)
			)
		.andExpect(status().isBadRequest())
		.andDo(print());
	}


	@Test
	@Disabled
	@WithUserDetails(value = "kakao_111111", setupBefore = TestExecutionEvent.TEST_EXECUTION)
	public void 수정_validation체크_임시저장글여부_누락() throws Exception {
		//given
		일정_참여상태_추가(createParticipant, ParticipantState.PARTICIPATION_COMPLETE_APPROVAL);
		Logboard createLogboard = 로그보드_추가(saveUser);
		
		MultiValueMap<String, String> info  = new LinkedMultiValueMap<>();
		info.add("content", "logboard test content"); 
		info.add("scheduleNo", String.valueOf(createSchedule.getScheduleNo()));
		info.add("isPublished", ""); // 임시 저장글 여부 누락

		//when & then
		mockMvc.perform(
				multipart("/logboard/edit/"+createLogboard.getLogboardNo())
				.file(getFakeMockMultipartFile())
				.file(getFakeMockMultipartFile())
				.file(getFakeMockMultipartFile())
				.params(info)
			)
		.andExpect(status().isBadRequest())
		.andDo(print());
	}
	
	@Test
	@Disabled
	@WithUserDetails(value = "kakao_111111", setupBefore = TestExecutionEvent.TEST_EXECUTION)
	public void 수정_없는_스케줄번호() throws Exception {
		//given
		일정_참여상태_추가(createParticipant, ParticipantState.PARTICIPATION_COMPLETE_APPROVAL);
		Logboard createLogboard = 로그보드_추가(saveUser);
		
		MultiValueMap<String, String> info  = new LinkedMultiValueMap<>();
		info.add("content", "logboard test content"); 
		info.add("scheduleNo", String.valueOf(100L));// 없는 스케줄번호
		info.add("isPublished", String.valueOf(true));

		//when & then
		mockMvc.perform(
				multipart("/logboard/edit/"+createLogboard.getLogboardNo())
				.file(getFakeMockMultipartFile())
				.file(getFakeMockMultipartFile())
				.file(getFakeMockMultipartFile())
				.params(info)
			)
		.andExpect(status().isBadRequest())
		.andDo(print());
	}


	@Test
	@Disabled
	@WithUserDetails(value = "kakao_111111", setupBefore = TestExecutionEvent.TEST_EXECUTION)
	public void 수정_일정_참여_중_상태일경우() throws Exception {
		//given
		일정_참여상태_추가(createParticipant, ParticipantState.PARTICIPATING);
		Logboard createLogboard = 로그보드_추가(saveUser);
		
		MultiValueMap<String, String> info  = new LinkedMultiValueMap<>();
		info.add("content", "logboard test content"); 
		info.add("scheduleNo", String.valueOf(createSchedule.getScheduleNo()));
		info.add("isPublished", String.valueOf(true));

		//then
		mockMvc.perform(
				multipart("/logboard/edit/"+createLogboard.getLogboardNo())
				.file(getFakeMockMultipartFile())
				.file(getFakeMockMultipartFile())
				.file(getFakeMockMultipartFile())
				.params(info)
			)
		.andExpect(status().isBadRequest())
		.andDo(print());
	}


	@Test
	@Disabled
	@WithUserDetails(value = "kakao_111111", setupBefore = TestExecutionEvent.TEST_EXECUTION)
	public void 수정_일정_참여_취소_요청_상태일경우() throws Exception {
		//given
		일정_참여상태_추가(createParticipant, ParticipantState.PARTICIPATION_CANCEL);
		Logboard createLogboard = 로그보드_추가(saveUser);
		
		MultiValueMap<String, String> info  = new LinkedMultiValueMap<>();
		info.add("content", "logboard test content"); 
		info.add("scheduleNo", String.valueOf(createSchedule.getScheduleNo()));
		info.add("isPublished", String.valueOf(true));

		//then
		mockMvc.perform(
				multipart("/logboard/edit/"+createLogboard.getLogboardNo())
				.file(getFakeMockMultipartFile())
				.file(getFakeMockMultipartFile())
				.file(getFakeMockMultipartFile())
				.params(info)
			)
		.andExpect(status().isBadRequest())
		.andDo(print());
	}

	
	@Test
	@Disabled
	@WithUserDetails(value = "kakao_111111", setupBefore = TestExecutionEvent.TEST_EXECUTION)
	public void 수정_일정_참여_취소_요청_승인_상태일경우() throws Exception {
		//given
		일정_참여상태_추가(createParticipant, ParticipantState.PARTICIPATION_CANCEL_APPROVAL);
		Logboard createLogboard = 로그보드_추가(saveUser);
		
		MultiValueMap<String, String> info  = new LinkedMultiValueMap<>();
		info.add("content", "logboard test content"); 
		info.add("scheduleNo", String.valueOf(createSchedule.getScheduleNo()));
		info.add("isPublished", String.valueOf(true));

		//then
		mockMvc.perform(
				multipart("/logboard/edit/"+createLogboard.getLogboardNo())
				.file(getFakeMockMultipartFile())
				.file(getFakeMockMultipartFile())
				.file(getFakeMockMultipartFile())
				.params(info)
			)
		.andExpect(status().isBadRequest())
		.andDo(print());
	}

	
	@Test
	@Disabled
	@WithUserDetails(value = "kakao_111111", setupBefore = TestExecutionEvent.TEST_EXECUTION)
	public void 수정_일정_참여_완료_미승인_상태일경우() throws Exception {
		//given
		일정_참여상태_추가(createParticipant, ParticipantState.PARTICIPATION_COMPLETE_UNAPPROVED);
		Logboard createLogboard = 로그보드_추가(saveUser);
		
		MultiValueMap<String, String> info  = new LinkedMultiValueMap<>();
		info.add("content", "logboard test content"); 
		info.add("scheduleNo", String.valueOf(createSchedule.getScheduleNo()));
		info.add("isPublished", String.valueOf(true));

		//then
		mockMvc.perform(
				multipart("/logboard/edit/"+createLogboard.getLogboardNo())
				.file(getFakeMockMultipartFile())
				.file(getFakeMockMultipartFile())
				.file(getFakeMockMultipartFile())
				.params(info)
			)
		.andExpect(status().isBadRequest())
		.andDo(print());
	}
	

	@Test
	@Disabled
	@WithUserDetails(value = "kakao_111111", setupBefore = TestExecutionEvent.TEST_EXECUTION)
	public void 수정_작성자확인_실패() throws Exception {
		//given
		User anotherUser = userRepository.save(User.builder()
                .id("kakao_222222")
                .password("2222")
                .nickName("nickname22")
                .email("email22@gmail.com")
                .gender(Gender.M)
                .birthDay(LocalDate.now())
                .picture("picture")
                .joinAlarmYn(true).beforeAlarmYn(true).noticeAlarmYn(true)
                .role(Role.USER)
                .provider("kakao")
                .providerId("222222")
                .build());
		
		일정_참여상태_추가(createParticipant, ParticipantState.PARTICIPATION_COMPLETE_APPROVAL);
		Logboard createLogboard = 로그보드_추가(anotherUser);

		MultiValueMap<String, String> info  = new LinkedMultiValueMap<>();
		info.add("content", "logboard test content"); 
		info.add("scheduleNo", String.valueOf(createSchedule.getScheduleNo()));
		info.add("isPublished", String.valueOf(true));

		//then
		mockMvc.perform(
				multipart("/logboard/edit/"+createLogboard.getLogboardNo())
				.file(getFakeMockMultipartFile())
				.file(getFakeMockMultipartFile())
				.file(getFakeMockMultipartFile())
				.params(info)
			)
		.andExpect(status().isForbidden())
		.andDo(print());
	}

	@Test
	@Disabled
	@WithUserDetails(value = "kakao_111111", setupBefore = TestExecutionEvent.TEST_EXECUTION)
	public void 수정_없는_로그번호_실패() throws Exception {
		//given
		일정_참여상태_추가(createParticipant, ParticipantState.PARTICIPATION_COMPLETE_APPROVAL);
		Logboard createLogboard = 로그보드_추가(saveUser);

		MultiValueMap<String, String> info  = new LinkedMultiValueMap<>();
		info.add("content", "logboard test content"); 
		info.add("scheduleNo", String.valueOf(createSchedule.getScheduleNo()));
		info.add("isPublished", String.valueOf(true));

		//then
		mockMvc.perform(
				multipart("/logboard/edit/10000")
				.file(getFakeMockMultipartFile())
				.file(getFakeMockMultipartFile())
				.file(getFakeMockMultipartFile())
				.params(info)
			)
		.andExpect(status().isBadRequest())
		.andDo(print());
	}
	
	
	
	@Test
	@WithUserDetails(value = "kakao_111111", setupBefore = TestExecutionEvent.TEST_EXECUTION)
	public void logboardDelete() throws Exception {
		//given
		일정_참여상태_추가(createParticipant, ParticipantState.PARTICIPATION_COMPLETE_APPROVAL);
		Logboard createLogboard = 로그보드_추가(saveUser);

		//when & then
		ResultActions result = mockMvc.perform(RestDocumentationRequestBuilders.delete("/logboard/{no}",
						createLogboard.getLogboardNo())
				.header(AUTHORIZATION_HEADER, "access Token")
		);

		result.andExpect(status().isOk())
				.andDo(print())
				.andDo(
						restDocs.document(
								requestHeaders(
										headerWithName(AUTHORIZATION_HEADER).description("JWT Access Token")
								),
								pathParameters(
										parameterWithName("no").description("봉사 로그 고유키 PK")
								)
						)
				);
	}

	@Test
	@Disabled
	@WithUserDetails(value = "kakao_111111", setupBefore = TestExecutionEvent.TEST_EXECUTION)
	public void 삭제_작성자확인_실패() throws Exception {
		//given
		User anotherUser = userRepository.save(User.builder()
                .id("kakao_222222")
                .password("2222")
                .nickName("nickname22")
                .email("email22@gmail.com")
                .gender(Gender.M)
                .birthDay(LocalDate.now())
                .picture("picture")
                .joinAlarmYn(true).beforeAlarmYn(true).noticeAlarmYn(true)
                .role(Role.USER)
                .provider("kakao")
                .providerId("222222")
                .build());
		
		일정_참여상태_추가(createParticipant, ParticipantState.PARTICIPATION_COMPLETE_APPROVAL);
		Logboard createLogboard = 로그보드_추가(anotherUser);
		
        //when & then
        mockMvc.perform(
                delete("/logboard/"+createLogboard.getLogboardNo()))
                .andExpect(status().isForbidden())
                .andDo(print());
	}

	@Test
	@Disabled
	@WithUserDetails(value = "kakao_111111", setupBefore = TestExecutionEvent.TEST_EXECUTION)
	public void 삭제_없는_로그번호_실패() throws Exception {
		//given
		일정_참여상태_추가(createParticipant, ParticipantState.PARTICIPATION_COMPLETE_APPROVAL);
		Logboard createLogboard = 로그보드_추가(saveUser);
		
        //when & then
        mockMvc.perform(
                delete("/logboard/10000"))
                .andExpect(status().isBadRequest())
                .andDo(print());
	}
	
	
	private ScheduleParticipation 일정_참여상태_추가(Participant participant, ParticipantState state){
		ScheduleParticipation sp = ScheduleParticipation.createScheduleParticipation(createSchedule, participant, state);
		return scheduleParticipationRepository.save(sp);
	}
	
	private Logboard 로그보드_추가(User user) throws Exception{
		Logboard logboard = Logboard.createLogBoard("test logboard contents", true, user.getUserNo());
		logboard.setWriter(user);
		logboard.setSchedule(createSchedule);
		
		Long logboardNo = logboardRepository.save(logboard).getLogboardNo();
		

		ImageParam uploadLogboardImg1 = new ImageParam(RealWorkCode.LOG, logboardNo, getMockMultipartFile());
		Long saveId1 = imageService.addImage(uploadLogboardImg1);

		ImageParam uploadLogboardImg2 = new ImageParam(RealWorkCode.LOG, logboardNo, getMockMultipartFile());
		Long saveId2 = imageService.addImage(uploadLogboardImg2);

		
		deleteS3ImageNoList.add(saveId1); // S3에 저장된 이미지 추후 삭제 예정
		deleteS3ImageNoList.add(saveId2); // S3에 저장된 이미지 추후 삭제 예정
		
		return logboard;
	}

	@AfterEach
	void deleteUploadImage(){
		List<Storage> storages = storageRepository.findAll();
		storages.stream().forEach(s -> fileService.deleteFile(s.getFakeImageName()));
	}
	
    
}