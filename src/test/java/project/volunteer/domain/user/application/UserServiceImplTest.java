package project.volunteer.domain.user.application;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import project.volunteer.domain.image.application.ImageService;
import project.volunteer.domain.image.application.dto.ImageParam;
import project.volunteer.domain.image.dao.ImageRepository;
import project.volunteer.domain.image.domain.Image;
import project.volunteer.global.common.component.RealWorkCode;
import project.volunteer.domain.recruitmentParticipation.repository.RecruitmentParticipationRepository;
import project.volunteer.domain.recruitmentParticipation.domain.RecruitmentParticipation;
import project.volunteer.domain.recruitment.application.RecruitmentCommandUseCase;
import project.volunteer.domain.recruitment.repository.RecruitmentRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.image.domain.Storage;
import project.volunteer.domain.user.api.dto.response.UserAlarmResponse;
import project.volunteer.domain.user.api.dto.response.UserJoinRequestListResponse;
import project.volunteer.domain.user.api.dto.response.UserRecruitingListResponse;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.dao.queryDto.dto.UserRecruitingQuery;
import project.volunteer.domain.user.dao.queryDto.dto.UserRecruitmentJoinRequestQuery;
import project.volunteer.domain.user.domain.Gender;
import project.volunteer.domain.user.domain.Role;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.*;
import project.volunteer.global.common.component.ParticipantState;
import project.volunteer.global.infra.s3.FileService;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Slf4j
public class UserServiceImplTest {

	@Autowired UserRepository userRepository;
	@Autowired
    RecruitmentParticipationRepository participantRepository;
	@Autowired RecruitmentRepository recruitmentRepository;
	@Autowired ImageRepository imageRepository;
	@Autowired
	RecruitmentCommandUseCase recruitmentService;
	@Autowired ImageService imageService;
	@Autowired FileService fileService;
	@Autowired UserService userService;
	@Autowired UserDtoService userDtoService;
	@PersistenceContext EntityManager em;

	String changedNickName= "changedNickName";
	String changedEmail= "changedEmail@test.test";
	
	private static User saveUser;
	private List<Long> deleteS3ImageNoList = new ArrayList<>();

    private MockMultipartFile getMockMultipartFile() throws IOException {
        return new MockMultipartFile(
                "file", "file.PNG", "image/jpg", new FileInputStream("src/main/resources/static/test/file.PNG"));
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
				.email(email3).gender(Gender.M).birthDay(LocalDate.now()).picture(picture3)
				.joinAlarmYn(true).beforeAlarmYn(true).noticeAlarmYn(true).role(Role.USER).provider("kakao")
				.providerId(providerId3).build());
        

		User userNo4 = userRepository.save(User.builder().id(id4).password("1234").nickName(nickName4)
				.email(email4).gender(Gender.M).birthDay(LocalDate.now()).picture(picture4)
				.joinAlarmYn(true).beforeAlarmYn(true).noticeAlarmYn(true).role(Role.USER).provider("kakao")
				.providerId(providerId4).build());


        //공통
        String title = "title";
        String content = "content";
        String organizationName = "organization";
        Timetable timetable = new Timetable(LocalDate.now(), LocalDate.now(), HourFormat.AM, LocalTime.now(), 10);
        Boolean isPublished = true;
        Coordinate coordinate = new Coordinate(3.2F, 3.2F);
        
        VolunteeringCategory category1 = VolunteeringCategory.ADMINSTRATION_ASSISTANCE;
        VolunteeringCategory category2 = VolunteeringCategory.CULTURAL_EVENT;
        VolunteeringCategory category3 = VolunteeringCategory.DISASTER;
        VolunteeringCategory category4 = VolunteeringCategory.EDUCATION;

        VolunteeringType volunteeringType1 = VolunteeringType.IRREG;
        VolunteeringType volunteeringType2 = VolunteeringType.REG;

        VolunteerType volunteerType1 = VolunteerType.ALL;
        VolunteerType volunteerType2 = VolunteerType.TEENAGER;
        VolunteerType volunteerType3 = VolunteerType.ADULT;

        Boolean isIssued1 = true;
        Boolean isIssued2 = false;

        
        String details = "details";
        Address address1 = new Address("11", "110011", details, "fullName");
        Address address2 = new Address("22", "220022", details, "fullName");
        Address address3 = new Address("33", "330033", details, "fullName");
        Address address4 = new Address("44", "440044", details, "fullName");

        int volunteerNum1 = 10;
		int volunteerNum2 = 20;
		int volunteerNum3 = 30;
		int volunteerNum4 = 40;

		Recruitment create1 = Recruitment.builder()
				.title(title).content(content).volunteeringCategory(category1).volunteeringType(volunteeringType1)
				.volunteerType(volunteerType1).maxParticipationNum(volunteerNum1).isIssued(isIssued1).organizationName(organizationName)
				.address(address1).coordinate(coordinate).timetable(timetable).isPublished(isPublished)
				.currentVolunteerNum(0).viewCount(0).likeCount(0).isDeleted(IsDeleted.N).writer(saveUser).writer(saveUser)
				.build();
		recruitmentRepository.save(create1);
		Long no1 = create1.getRecruitmentNo();

		Recruitment create2 = Recruitment.builder()
				.title(title).content(content).volunteeringCategory(category2).volunteeringType(volunteeringType2)
				.volunteerType(volunteerType2).maxParticipationNum(volunteerNum2).isIssued(isIssued2).organizationName(organizationName)
				.address(address2).coordinate(coordinate).timetable(timetable).isPublished(isPublished)
				.currentVolunteerNum(0).viewCount(0).likeCount(0).isDeleted(IsDeleted.N).writer(saveUser)
				.build();
		recruitmentRepository.save(create2);
		Long no2 = create2.getRecruitmentNo();

		Recruitment create3 = Recruitment.builder()
				.title(title).content(content).volunteeringCategory(category3).volunteeringType(volunteeringType1)
				.volunteerType(volunteerType3).maxParticipationNum(volunteerNum3).isIssued(isIssued1).organizationName(organizationName)
				.address(address3).coordinate(coordinate).timetable(timetable).isPublished(isPublished)
				.currentVolunteerNum(0).viewCount(0).likeCount(0).isDeleted(IsDeleted.N).writer(userNo2)
				.build();
		recruitmentRepository.save(create3);
		Long no3 = create3.getRecruitmentNo();

		Recruitment create4 = Recruitment.builder()
				.title(title).content(content).volunteeringCategory(category4).volunteeringType(volunteeringType2)
				.volunteerType(volunteerType1).maxParticipationNum(volunteerNum4).isIssued(isIssued2).organizationName(organizationName)
				.address(address4).coordinate(coordinate).timetable(timetable).isPublished(isPublished)
				.currentVolunteerNum(0).viewCount(0).likeCount(0).isDeleted(IsDeleted.N).writer(userNo3)
				.build();
		recruitmentRepository.save(create4);
		Long no4 = create4.getRecruitmentNo();
		
		// upload 이미지 저장
		ImageParam uploadImageDto = ImageParam.builder().code(RealWorkCode.RECRUITMENT).no(no2).uploadImage(getMockMultipartFile()).build();
		Long saveId2 = imageService.addImage(uploadImageDto);
		deleteS3ImageNoList.add(saveId2); // S3에 저장된 이미지 추후 삭제 예정
	
	
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


		Recruitment recruitment7 = recruitmentRepository.findById(no3).get();
		RecruitmentParticipation participant7 = RecruitmentParticipation.builder().participant(saveUser).recruitment(recruitment7)
				.state(ParticipantState.JOIN_REQUEST)
				.build();
		participantRepository.save(participant7);
		
		Recruitment recruitment8 = recruitmentRepository.findById(no4).get();
		RecruitmentParticipation participant8 = RecruitmentParticipation.builder().participant(saveUser).recruitment(recruitment8)
				.state(ParticipantState.JOIN_REQUEST)
				.build();
		participantRepository.save(participant8);
		
		clear();

		// init 데이터 요약
		/*
			사용자 4명 생성
			모집글 4개 생성(staticImg, S3업로드Img 3개)
			
			참여자 요약
			사용자 1 : 모집글 1번, 2번 승인(모임장), 3번, 4번 신청
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
	void 나의_모집글_승인대기_리스트조회() throws Exception{
		UserJoinRequestListResponse result = userDtoService.findUserJoinRequest(saveUser.getUserNo());
		List<UserRecruitmentJoinRequestQuery> dataList = result.getRequestList();
		
		Assertions.assertThat(dataList.size()).isEqualTo(2);
	}
	
	@Test
	void 나의_모집글_모집중_리스트조회() throws Exception{
		UserRecruitingListResponse result = userDtoService.findUserRecruiting(saveUser.getUserNo());
		List<UserRecruitingQuery> dataList = result.getRecruitingList();
		Assertions.assertThat(dataList.size()).isEqualTo(2);
		dataList.stream().forEach(data -> {
			log.info("CurrentVolunteerNum={}",data.getCurrentVolunteerNum());
		});
	}
	
	@Test
	void 나의_메일수신동의여부_조회() throws Exception{
		UserAlarmResponse result = userService.findUserAlarm(saveUser.getUserNo());
		Assertions.assertThat(result.getJoinAlarm().TRUE);
		Assertions.assertThat(result.getNoticeAlarm().TRUE);
		Assertions.assertThat(result.getBeforeAlarm().TRUE);
	}
	

	@Test
	void 나의_메일수신동의여부_수정() throws Exception{
		userService.userAlarmUpdate(saveUser.getUserNo(),Boolean.FALSE, Boolean.FALSE, Boolean.FALSE);
		UserAlarmResponse result = userService.findUserAlarm(saveUser.getUserNo());
		Assertions.assertThat(result.getJoinAlarm().FALSE);
		Assertions.assertThat(result.getNoticeAlarm().FALSE);
		Assertions.assertThat(result.getBeforeAlarm().FALSE);
	}

	@Test
	void 프로필_수정_프로필사진변경() throws Exception{
		userService.userInfoUpdate(saveUser.getUserNo(), changedNickName, changedEmail, "https://test.png");
		Optional<User> result = userRepository.findById(saveUser.getUserNo());

		Assertions.assertThat(result.get().getNickName().equals(changedNickName));
		Assertions.assertThat(result.get().getEmail().equals(changedEmail));
		Assertions.assertThat(!result.get().getPicture().equals(saveUser.getPicture()));
	}

	@Test
	void 프로필_수정_프로필사진변경X() throws Exception{
		userService.userInfoUpdate(saveUser.getUserNo(), changedNickName, changedEmail, null);
		Optional<User> result = userRepository.findById(saveUser.getUserNo());
		
		Assertions.assertThat(result.get().getNickName().equals(changedNickName));
		Assertions.assertThat(result.get().getEmail().equals(changedEmail));
		Assertions.assertThat(result.get().getPicture().equals(saveUser.getPicture()));
	}
}
