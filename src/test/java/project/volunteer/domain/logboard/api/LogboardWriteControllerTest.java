package project.volunteer.domain.logboard.api;

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
import java.time.LocalTime;
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
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import project.volunteer.domain.image.application.ImageService;
import project.volunteer.domain.image.dao.ImageRepository;
import project.volunteer.domain.image.dao.StorageRepository;
import project.volunteer.domain.image.domain.Storage;
import project.volunteer.domain.logboard.domain.Logboard;
import project.volunteer.domain.participation.dao.ParticipantRepository;
import project.volunteer.domain.participation.domain.Participant;
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
import project.volunteer.global.common.component.Timetable;
import project.volunteer.global.infra.s3.FileService;
import project.volunteer.document.restdocs.config.RestDocsConfiguration;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@AutoConfigureRestDocs
@Import(RestDocsConfiguration.class)
public class LogboardWriteControllerTest {
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
	@Autowired ScheduleParticipationRepository scheduleParticipationRepository;
	@Autowired UserDtoService userDtoService;
	@Autowired StorageRepository storageRepository;
	@Autowired RestDocumentationResultHandler restDocs;
	@PersistenceContext EntityManager em;

	private static User saveUser;
	private static Schedule schedule1;
	private static Schedule schedule2;
	private static Schedule schedule3;
	private static Schedule schedule4;
	private static Schedule schedule5;
	private static ScheduleParticipation scheduleParticipation1;
	private static ScheduleParticipation scheduleParticipation2;
	private static ScheduleParticipation scheduleParticipation3;
	private static ScheduleParticipation scheduleParticipation4;
	private static ScheduleParticipation scheduleParticipation5;

	final String AUTHORIZATION_HEADER = "accessToken";

	private MockMultipartFile getFakeMockMultipartFile() throws IOException {
		return new MockMultipartFile(
				"picture", "".getBytes());
	}

	private MockMultipartFile getRealMockMultipartFile() throws IOException {
		return new MockMultipartFile(
				"picture", "file.PNG", "image/jpg", new FileInputStream("src/main/resources/static/test/file.PNG"));
	}

	private void clear() {
		em.flush();
		em.clear();
	}

	@BeforeEach
	public void initUser() throws Exception{
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

		// 유저 추가
		String id2 = "kakao_222222";
		String nickName2 = "nickname2";
		String email2 = "email22@gmail.com";
		String picture2 = "picture2";
		String providerId2 = "222222";

		User userNo2 = userRepository.save(User.builder().id(id2).password("1234").nickName(nickName2)
				.email(email2).gender(Gender.M).birthDay(LocalDate.now()).picture(picture2)
				.joinAlarmYn(true).beforeAlarmYn(true).noticeAlarmYn(true).role(Role.USER).provider("kakao")
				.providerId(providerId2).build());

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
		Participant participant1 = Participant.createParticipant(recruitment, saveUser, ParticipantState.JOIN_APPROVAL);
		participantRepository.save(participant1);

		// user2 참여자 저장
		Participant participant2 = Participant.createParticipant(recruitment, userNo2, ParticipantState.JOIN_APPROVAL);
		participantRepository.save(participant2);

		// 스케줄 저장
		schedule1 = Schedule.create(recruitment, timetable, content, organizationName, address, volunteerNum);
		scheduleRepository.save(schedule1);

		schedule2 = Schedule.create(recruitment, timetable, content, organizationName, address, volunteerNum);
		scheduleRepository.save(schedule2);

		schedule3 = Schedule.create(recruitment, timetable, content, organizationName, address, volunteerNum);
		scheduleRepository.save(schedule3);

		schedule4 = Schedule.create(recruitment, timetable, content, organizationName, address, volunteerNum);
		scheduleRepository.save(schedule4);

		schedule5 = Schedule.create(recruitment, timetable, content, organizationName, address, volunteerNum);
		scheduleRepository.save(schedule5);

		// 방장 스케줄 참여
		scheduleParticipation1 = ScheduleParticipation.createScheduleParticipation(schedule1, participant1, ParticipantState.PARTICIPATION_COMPLETE_APPROVAL);
		scheduleParticipationRepository.save(scheduleParticipation1);

		scheduleParticipation2 = ScheduleParticipation.createScheduleParticipation(schedule2, participant1, ParticipantState.PARTICIPATING);
		scheduleParticipationRepository.save(scheduleParticipation2);

		scheduleParticipation3 = ScheduleParticipation.createScheduleParticipation(schedule3, participant1, ParticipantState.PARTICIPATION_CANCEL);
		scheduleParticipationRepository.save(scheduleParticipation3);

		scheduleParticipation4 = ScheduleParticipation.createScheduleParticipation(schedule4, participant1, ParticipantState.PARTICIPATION_CANCEL_APPROVAL);
		scheduleParticipationRepository.save(scheduleParticipation4);

		scheduleParticipation5 = ScheduleParticipation.createScheduleParticipation(schedule5, participant1, ParticipantState.PARTICIPATION_COMPLETE_UNAPPROVED);
		scheduleParticipationRepository.save(scheduleParticipation5);


		Logboard logboard = Logboard.createLogBoard(content, isPublished, saveUser.getUserNo());
		logboard.setWriter(saveUser);

		clear();

		// init 데이터 요약
		/*
			사용자 2명 생성
			모집글 1개 생성(staticImg)
			스케쥴 5개 생성

			참여자 요약
			사용자 1 : 모집글 승인(모임장)
			사용자 2 : 모집글 승인

			스케줄 참여자 요약
			스케줄 참여 5개 : 사용자1 모두 참여
			스케줄 참여 1 : 일정 참여 완료 승인
			스케줄 참여 2 : 일정 참여 중
			스케줄 참여 3 : 일정 참여 취소 요청
			스케줄 참여 4 : 일정 참여 취소 요청 승인
			스케줄 참여 5 : 일정 참여 완료 미승인

			로그 작성
			스케줄 참여 2에 사용자 1이 작성
		 */
	}



	@Test
	@WithUserDetails(value = "kakao_111111", setupBefore = TestExecutionEvent.TEST_EXECUTION)
	public void logboardWrite() throws Exception {
		//given
		MultiValueMap<String, String> info  = new LinkedMultiValueMap<>();
		info.add("content", "logboard test content");
		info.add("scheduleNo", String.valueOf(schedule1.getScheduleNo()));
		info.add("isPublished", String.valueOf(true));

		//when & then
		ResultActions result = mockMvc.perform(
				multipart("/logboard")
						.file(getFakeMockMultipartFile())
						.file(getFakeMockMultipartFile())
						.file(getFakeMockMultipartFile())
						.header(AUTHORIZATION_HEADER, "access Token")
						.params(info)
		);

		result.andExpect(status().isCreated())
				.andDo(print())
				.andDo(
						restDocs.document(
								requestHeaders(
										headerWithName(AUTHORIZATION_HEADER).description("JWT Access Token")
								),
								requestParts(
										partWithName("picture").optional().description("작성할 봉사 로그 이미지")
								),
								requestParameters(
										parameterWithName("content").description("봉사 로그 내용"),
										parameterWithName("scheduleNo").description("봉사 참여 고유키 PK"),
										parameterWithName("isPublished").description("봉사 로그 발행 여부")
								)
						)
				);
	}


	@Test
	@Disabled
	@WithUserDetails(value = "kakao_111111", setupBefore = TestExecutionEvent.TEST_EXECUTION)
	public void validation체크_내용_누락() throws Exception {
		//given
		MultiValueMap<String, String> info  = new LinkedMultiValueMap<>();
		info.add("content", ""); // 내용 누락
		info.add("scheduleNo", String.valueOf(schedule1.getScheduleNo()));
		info.add("isPublished", String.valueOf(true));

		//when & then
		mockMvc.perform(
						multipart("/logboard")
								.file(getRealMockMultipartFile())
								.file(getRealMockMultipartFile())
								.file(getRealMockMultipartFile())
								.params(info)
				)
				.andExpect(status().isBadRequest())
				.andDo(print());
	}

	@Test
	@Disabled
	@WithUserDetails(value = "kakao_111111", setupBefore = TestExecutionEvent.TEST_EXECUTION)
	public void validation체크_스케줄번호_누락() throws Exception {
		//given
		MultiValueMap<String, String> info  = new LinkedMultiValueMap<>();
		info.add("content", "logboard test content");
		info.add("scheduleNo", "");// 스케줄번호 누락
		info.add("isPublished", String.valueOf(true));

		//when & then
		mockMvc.perform(
						multipart("/logboard")
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
	public void validation체크_임시저장글여부_누락() throws Exception {
		//given
		MultiValueMap<String, String> info  = new LinkedMultiValueMap<>();
		info.add("content", "logboard test content");
		info.add("scheduleNo", String.valueOf(schedule1.getScheduleNo()));
		info.add("isPublished", ""); // 임시 저장글 여부 누락
		//when & then
		mockMvc.perform(
						multipart("/logboard")
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
	public void 없는_스케줄번호() throws Exception {
		//given
		MultiValueMap<String, String> info  = new LinkedMultiValueMap<>();
		info.add("content", "logboard test content");
		info.add("scheduleNo", String.valueOf(100L));// 없는 스케줄번호
		info.add("isPublished", String.valueOf(true));

		//when & then
		mockMvc.perform(
						multipart("/logboard")
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
	public void 이미_작성한_로그일_경우() throws Exception {
		//given
		MultiValueMap<String, String> info  = new LinkedMultiValueMap<>();
		info.add("content", "logboard test content");
		info.add("scheduleNo", String.valueOf(schedule2.getScheduleNo()));
		info.add("isPublished", String.valueOf(true));

		//when & then
		mockMvc.perform(
						multipart("/logboard")
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
	public void 일정_참여_중_상태일경우() throws Exception {
		/*
			스케줄 참여 1 : 일정 참여 완료 승인
			스케줄 참여 2 : 일정 참여 중
			스케줄 참여 3 : 일정 참여 취소 요청
			스케줄 참여 4 : 일정 참여 취소 요청 승인
			스케줄 참여 5 : 일정 참여 완료 미승인
		 */
		//given & when
		MultiValueMap<String, String> info  = new LinkedMultiValueMap<>();
		info.add("content", "logboard test content");
		info.add("scheduleNo", String.valueOf(schedule2.getScheduleNo()));
		info.add("isPublished", String.valueOf(true));

		//then
		mockMvc.perform(
						multipart("/logboard")
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
	public void 일정_참여_취소_요청_상태일경우() throws Exception {
		/*
			스케줄 참여 1 : 일정 참여 완료 승인
			스케줄 참여 2 : 일정 참여 중
			스케줄 참여 3 : 일정 참여 취소 요청
			스케줄 참여 4 : 일정 참여 취소 요청 승인
			스케줄 참여 5 : 일정 참여 완료 미승인
		 */
		//given & when
		MultiValueMap<String, String> info  = new LinkedMultiValueMap<>();
		info.add("content", "logboard test content");
		info.add("scheduleNo", String.valueOf(schedule3.getScheduleNo()));
		info.add("isPublished", String.valueOf(true));

		//then
		mockMvc.perform(
						multipart("/logboard")
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
	public void 일정_참여_취소_요청_승인_상태일경우() throws Exception {
		/*
			스케줄 참여 1 : 일정 참여 완료 승인
			스케줄 참여 2 : 일정 참여 중
			스케줄 참여 3 : 일정 참여 취소 요청
			스케줄 참여 4 : 일정 참여 취소 요청 승인
			스케줄 참여 5 : 일정 참여 완료 미승인
		 */
		//given & when
		MultiValueMap<String, String> info  = new LinkedMultiValueMap<>();
		info.add("content", "logboard test content");
		info.add("scheduleNo", String.valueOf(schedule4.getScheduleNo()));
		info.add("isPublished", String.valueOf(true));

		//then
		mockMvc.perform(
						multipart("/logboard")
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
	public void 일정_참여_완료_미승인_상태일경우() throws Exception {
		/*
			스케줄 참여 1 : 일정 참여 완료 승인
			스케줄 참여 2 : 일정 참여 중
			스케줄 참여 3 : 일정 참여 취소 요청
			스케줄 참여 4 : 일정 참여 취소 요청 승인
			스케줄 참여 5 : 일정 참여 완료 미승인
		 */
		//given & when
		MultiValueMap<String, String> info  = new LinkedMultiValueMap<>();
		info.add("content", "logboard test content");
		info.add("scheduleNo", String.valueOf(schedule5.getScheduleNo()));
		info.add("isPublished", String.valueOf(true));

		//then
		mockMvc.perform(
						multipart("/logboard")
								.file(getFakeMockMultipartFile())
								.file(getFakeMockMultipartFile())
								.file(getFakeMockMultipartFile())
								.params(info)
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
