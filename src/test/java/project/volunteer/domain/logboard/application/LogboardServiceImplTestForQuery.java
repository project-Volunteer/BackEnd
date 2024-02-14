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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import project.volunteer.domain.image.application.ImageService;
import project.volunteer.domain.image.application.dto.ImageParam;
import project.volunteer.domain.image.dao.ImageRepository;
import project.volunteer.domain.image.domain.Image;
import project.volunteer.domain.logboard.application.dto.LogboardEditDetail;
import project.volunteer.domain.logboard.dao.LogboardRepository;
import project.volunteer.domain.logboard.dao.dto.LogboardListQuery;
import project.volunteer.domain.logboard.domain.Logboard;
import project.volunteer.domain.participation.dao.ParticipantRepository;
import project.volunteer.domain.participation.domain.Participant;
import project.volunteer.domain.recruitment.application.RecruitmentCommandUseCase;
import project.volunteer.domain.recruitment.repository.RecruitmentRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
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
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;
import project.volunteer.global.infra.s3.FileService;

@SpringBootTest
@Transactional
public class LogboardServiceImplTestForQuery {
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
	@Autowired LogboardService logboardService;
	@Autowired LogboardRepository logboardRepository;
	@PersistenceContext EntityManager em;

	List<Logboard> logboardList= new ArrayList<>();
	
	private static User saveUser;
	private static User saveUser2;
	private List<Long> deleteS3ImageNoList = new ArrayList<>();

    private MockMultipartFile getMockMultipartFile(String i) throws IOException {
        return new MockMultipartFile(
                "file"+i, "file"+i+".PNG", "image/jpg", new FileInputStream("src/main/resources/static/test/file.PNG"));
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
		

		// 스케줄 저장
		Schedule createSchedule =
				Schedule.create(recruitment, timetable, content, organizationName, address, volunteerNum);
		scheduleRepository.save(createSchedule);
		
        // 봉사 로그 저장(한 모집글에 20개)
        for(int i = 0; i < 20; i++){
    		Logboard logboard = Logboard.createLogBoard(content+i+i+i+i, isPublished,saveUser.getUserNo());
    		if(i%2 == 1) {
        		logboard.setWriter(saveUser);
    		}else {
        		logboard.setWriter(saveUser2);
    		}
    		
    		logboard.setSchedule(createSchedule);
        	logboardList.add(logboard);
    		
    		Long logboardNo = logboardRepository.save(logboard).getLogboardNo();

			ImageParam uploadLogboardImg1 = new ImageParam(RealWorkCode.LOG, logboardNo, getMockMultipartFile(i+"_1"));
			Long saveLogboardImgId1 = imageService.addImage(uploadLogboardImg1);

			ImageParam uploadLogboardImg2 = new ImageParam(RealWorkCode.LOG, logboardNo, getMockMultipartFile(i+"_2"));
			Long saveLogboardImgId2 = imageService.addImage(uploadLogboardImg2);

			deleteS3ImageNoList.add(saveLogboardImgId1); // S3에 저장된 이미지 추후 삭제 예정
			deleteS3ImageNoList.add(saveLogboardImgId2); // S3에 저장된 이미지 추후 삭제 예정
        }
		
		clear();

		// init 데이터 요약
		/*
			사용자 2명 생성
			모집글 1개 생성(staticImg)
			스케쥴 1개 생성
			로그 20개 생성
			
			사용자 1의 logboardNo array = [2, 4, 6, 8, 10, 12, 14, 16, 18, 20]
			사용자 2의 logboardNo array = [1, 3, 5, 7, 9, 11, 13, 15, 17, 19]
		 */
	}

	@Test
	void 로그보드_조회() throws Exception {
		LogboardEditDetail logboardDetails = logboardService.findLogboard(logboardList.get(0).getLogboardNo());
		
		Assertions.assertThat(logboardDetails.getContent().equals("content0000"));
	}
	
	@Test
	void 로그보드_조회_없는_로그조회_실패() throws Exception {
		assertThatThrownBy(() -> logboardService.findLogboard(100000L))
			.isInstanceOf(BusinessException.class)
			.hasMessageContaining(ErrorCode.NOT_EXIST_LOGBOARD.name());
	}
	
	//TODO : 댓글수 관련 기능 미완성
	@Test
	void 로그보드_리스트_조회_사용자1() throws Exception {
		//given
		String searchType = "mylog"; // all, mylog
		PageRequest page = PageRequest.of(0, 6);
		
		//when
		Slice<LogboardListQuery> firstResult = 
				logboardRepository.findLogboardDtos(page, searchType, saveUser.getUserNo(), null);
		
		Slice<LogboardListQuery> result = 
				logboardRepository.findLogboardDtos(page, searchType, saveUser.getUserNo(), firstResult.toList().get(5).getNo());
		
		//then
		Assertions.assertThat(result.getContent().size()).isEqualTo(4);
		Assertions.assertThat(result.hasNext()).isFalse();
		Assertions.assertThat(result.getNumber()).isEqualTo(0);
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
