package project.volunteer.domain.logboard.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import project.volunteer.domain.image.application.ImageService;
import project.volunteer.domain.image.application.dto.ImageParam;
import project.volunteer.domain.image.dao.ImageRepository;
import project.volunteer.domain.image.domain.Image;
import project.volunteer.domain.image.domain.ImageType;
import project.volunteer.domain.image.domain.RealWorkCode;
import project.volunteer.domain.logboard.dao.LogboardRepository;
import project.volunteer.domain.logboard.domain.Logboard;
import project.volunteer.domain.participation.dao.ParticipantRepository;
import project.volunteer.domain.participation.domain.Participant;
import project.volunteer.domain.recruitment.application.RecruitmentService;
import project.volunteer.domain.recruitment.dao.RecruitmentRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.scheduleParticipation.dao.ScheduleParticipationRepository;
import project.volunteer.domain.scheduleParticipation.domain.ScheduleParticipation;
import project.volunteer.domain.sehedule.dao.ScheduleRepository;
import project.volunteer.domain.sehedule.domain.Schedule;
import project.volunteer.domain.storage.domain.Storage;
import project.volunteer.domain.user.application.UserDtoService;
import project.volunteer.domain.user.application.UserService;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.domain.Gender;
import project.volunteer.domain.user.domain.Role;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.Address;
import project.volunteer.global.common.component.Coordinate;
import project.volunteer.global.common.component.HourFormat;
import project.volunteer.global.common.component.ParticipantState;
import project.volunteer.global.common.component.Timetable;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;
import project.volunteer.global.infra.s3.FileService;

@SpringBootTest
public class LogboardServiceImplTestForSave {
	@Autowired UserRepository userRepository;
	@Autowired ParticipantRepository participantRepository;
	@Autowired RecruitmentRepository recruitmentRepository;
	@Autowired ImageRepository imageRepository;
	@Autowired RecruitmentService recruitmentService;
	@Autowired ImageService imageService;
	@Autowired FileService fileService;
	@Autowired UserService userService;
	@Autowired LogboardService logboardService;
	@Autowired ScheduleRepository scheduleRepository;
	@Autowired UserDtoService userDtoService;
    @Autowired ScheduleParticipationRepository scheduleParticipationRepository;
	@Autowired LogboardRepository logboardRepository;
	@PersistenceContext EntityManager em;

	List<Logboard> logboardList= new ArrayList<>();
	
	private static User saveUser;
	private static User saveUser2;
	private static Schedule createSchedule;
	private static Participant createParticipant;
	private static Participant createParticipant2;
	
	private void clear() {
		em.flush();
		em.clear();
	}

	private List<Long> deleteS3ImageNoList = new ArrayList<>();
	
    private MockMultipartFile getMockMultipartFile() throws IOException {
        return new MockMultipartFile(
                "file", "file.PNG", "image/jpg", new FileInputStream("src/main/resources/static/test/file.PNG"));
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
        
        saveUser2 = userRepository.save(User.builder()
                .id("kakao_2222")
                .password("1234")
                .nickName("nickname22")
                .email("email22@gmail.com")
                .gender(Gender.W)
                .birthDay(LocalDate.now())
                .picture("picture")
                .joinAlarmYn(true).beforeAlarmYn(true).noticeAlarmYn(true)
                .role(Role.USER)
                .provider("kakao")
                .providerId("2222")
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
        Address address = new Address("11", "110011", details);
        int volunteerNum = 10;

		Recruitment createRecruitment = Recruitment.builder()
				.title(title).content(content).volunteeringCategory(category).volunteeringType(volunteeringType)
				.volunteerType(volunteerType).volunteerNum(volunteerNum).isIssued(isIssued).organizationName(organizationName)
				.address(address).coordinate(coordinate).timetable(timetable).isPublished(isPublished)
				.build();
		createRecruitment.setWriter(saveUser);
		recruitmentRepository.save(createRecruitment);
		Long no = createRecruitment.getRecruitmentNo();

		// static 이미지 저장
		ImageParam staticImageDto1 = ImageParam.builder().code(RealWorkCode.RECRUITMENT).imageType(ImageType.STATIC)
				.no(no).staticImageCode("imgname1").uploadImage(null).build();
		imageService.addImage(staticImageDto1);

		// 방장 참여자 저장
		createParticipant = Participant.builder()
				.recruitment(createRecruitment)
				.participant(saveUser)
				.state(ParticipantState.JOIN_APPROVAL)
				.build();
		participantRepository.save(createParticipant);
		

		// 신규 참여자 저장
		createParticipant2 = Participant.builder()
				.recruitment(createRecruitment)
				.participant(saveUser2)
				.state(ParticipantState.JOIN_REQUEST)
				.build();
		participantRepository.save(createParticipant2);

		// 스케줄 저장
		createSchedule = Schedule.createSchedule(timetable, content, organizationName, address, volunteerNum);
		createSchedule.setRecruitment(createRecruitment);
		scheduleRepository.save(createSchedule);
		
		clear();

		// init 데이터 요약
		/*
			사용자 2명 생성
			모집글 1개 생성(staticImg)
			스케쥴 1개 생성
			
			참여자 요약
			사용자 1 : 모집글 승인(모임장)
			사용자 2 : 모집글 승인요청
		 */
    }
    
	@Test
    @Transactional
	void 로그저장_성공() {
		// given
		일정_참여상태_추가(createParticipant, ParticipantState.PARTICIPATION_COMPLETE_APPROVAL);
		
		// when
		Long logboardNo = logboardService.addLog(saveUser.getUserNo(), "test contents", createSchedule.getScheduleNo(), true);
		Logboard savedLogboard= logboardRepository.findById(logboardNo).get();

		// then
		Assertions.assertThat(savedLogboard.getLikeCount().equals(0));
		Assertions.assertThat(savedLogboard.getViewCount().equals(0));
		Assertions.assertThat(savedLogboard.getCreateUserNo().equals(saveUser.getUserNo()));
		Assertions.assertThat(!savedLogboard.getCreatedDate().toString().isEmpty());
	}

	@Test
    @Transactional
	void 로그저장_없는_사용자_요청으로_실패() {
		// given
		일정_참여상태_추가(createParticipant, ParticipantState.PARTICIPATION_COMPLETE_APPROVAL);

        //when & then
        assertThatThrownBy(() -> logboardService.addLog(20000L, "test contents", createSchedule.getScheduleNo(), true))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.NOT_EXIST_USER.name());
	}

	@Test
    @Transactional
	void 로그저장_없는스케줄_요청으로_실패() {
		// given
		일정_참여상태_추가(createParticipant, ParticipantState.PARTICIPATING);

        //when & then
        assertThatThrownBy(() -> logboardService.addLog(saveUser.getUserNo(), "test contents", 20000L, true))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.NOT_EXIST_SCHEDULE.name());
	}

	@Test
    @Transactional
	void 로그_중복작성으로_실패() throws Exception {
		// given
		일정_참여상태_추가(createParticipant, ParticipantState.PARTICIPATION_COMPLETE_APPROVAL);
		Logboard logboard = 로그보드_추가(saveUser);
		
        //when & then
        assertThatThrownBy(() -> logboardService.addLog(logboard.getWriter().getUserNo(), "test contents", logboard.getSchedule().getScheduleNo(), true))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.DUPLICATE_LOGBOARD.name());
	}

	@Test
    @Transactional
	void 로그저장_참여중상태로_실패() {
		// given
		일정_참여상태_추가(createParticipant, ParticipantState.PARTICIPATING);

        //when & then
        assertThatThrownBy(() -> logboardService.addLog(saveUser.getUserNo(), "test contents", createSchedule.getScheduleNo(), true))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.INVALID_STATE_LOGBOARD.name());
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
		

		ImageParam uploadLogboardImg1 = new ImageParam(RealWorkCode.LOG, logboardNo, ImageType.UPLOAD, null, getMockMultipartFile());
		Long saveId1 = imageService.addImage(uploadLogboardImg1);

		ImageParam uploadLogboardImg2 = new ImageParam(RealWorkCode.LOG, logboardNo, ImageType.UPLOAD, null, getMockMultipartFile());
		Long saveId2 = imageService.addImage(uploadLogboardImg2);

		
		deleteS3ImageNoList.add(saveId1); // S3에 저장된 이미지 추후 삭제 예정
		deleteS3ImageNoList.add(saveId2); // S3에 저장된 이미지 추후 삭제 예정
		
		return logboard;
	}
	
	@AfterEach
	public void deleteS3Image() { // S3에 테스트를 위해 저장한 이미지 삭제
		for (Long id : deleteS3ImageNoList) {
			Image image = imageRepository.findById(id).get();
			Storage storage = image.getStorage();
			fileService.deleteFile(storage.getFakeImageName());
		}
	}
}
