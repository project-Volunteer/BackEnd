package project.volunteer.domain.logboard.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.image.application.ImageService;
import project.volunteer.domain.image.application.dto.ImageParam;
import project.volunteer.domain.image.dao.ImageRepository;
import project.volunteer.domain.like.dao.LikeRepository;
import project.volunteer.domain.like.domain.Like;
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
import project.volunteer.domain.reply.application.ReplyService;
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
import project.volunteer.global.common.component.RealWorkCode;
import project.volunteer.global.common.component.Timetable;
import project.volunteer.global.infra.s3.FileService;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class LogboardContollerTestForQuery {
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
	@Autowired ReplyService replyService;
	@Autowired ScheduleRepository scheduleRepository;
	@Autowired UserDtoService userDtoService;
	@Autowired LogboardRepository logboardRepository;
	@Autowired ScheduleParticipationRepository scheduleParticipationRepository;
	@Autowired LikeRepository likeRepository;
	@PersistenceContext EntityManager em;

	List<Logboard> logboardList= new ArrayList<>();
	
	private static User saveUser;
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

        String title = "Recuritment title";
        String content = "content";
        String organizationName = "organization";
        Timetable timetable = new Timetable(LocalDate.now(), LocalDate.now(), HourFormat.AM, LocalTime.now(), 10);
        Timetable timetable1 = new Timetable(LocalDate.now(), LocalDate.now().minusDays(1), HourFormat.AM, LocalTime.now(), 10);
        Timetable timetable2 = new Timetable(LocalDate.now(), LocalDate.now().minusDays(2), HourFormat.AM, LocalTime.now(), 10);
        Timetable timetable3 = new Timetable(LocalDate.now(), LocalDate.now().minusDays(3), HourFormat.AM, LocalTime.now(), 10);
        Timetable timetable4 = new Timetable(LocalDate.now(), LocalDate.now().minusDays(4), HourFormat.AM, LocalTime.now(), 10);
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
		

		// 스케줄 저장
		Schedule schedule1 = Schedule.create(recruitment, timetable1, content, organizationName, address, volunteerNum);
		scheduleRepository.save(schedule1);

		Schedule schedule2 = Schedule.create(recruitment, timetable2, content, organizationName, address, volunteerNum);
		scheduleRepository.save(schedule2);

		Schedule schedule3 = Schedule.create(recruitment, timetable3, content, organizationName, address, volunteerNum);
		scheduleRepository.save(schedule3);
		
		Schedule schedule4 = Schedule.create(recruitment, timetable4, content, organizationName, address, volunteerNum);
		scheduleRepository.save(schedule4);

		Schedule schedule5 = Schedule.create(recruitment, timetable, content, organizationName, address, volunteerNum);
		scheduleRepository.save(schedule5);
		
		// 방장 스케줄 참여
		ScheduleParticipation scheduleParticipation1 = ScheduleParticipation.createScheduleParticipation(schedule1, participant1, ParticipantState.PARTICIPATION_COMPLETE_APPROVAL);
		scheduleParticipationRepository.save(scheduleParticipation1);

		ScheduleParticipation scheduleParticipation2 = ScheduleParticipation.createScheduleParticipation(schedule2, participant1, ParticipantState.PARTICIPATION_COMPLETE_APPROVAL);
		scheduleParticipationRepository.save(scheduleParticipation2);

		ScheduleParticipation scheduleParticipation3 = ScheduleParticipation.createScheduleParticipation(schedule3, participant1, ParticipantState.PARTICIPATION_COMPLETE_APPROVAL);
		scheduleParticipationRepository.save(scheduleParticipation3);

		ScheduleParticipation scheduleParticipation4 = ScheduleParticipation.createScheduleParticipation(schedule4, participant1, ParticipantState.PARTICIPATION_COMPLETE_APPROVAL);
		scheduleParticipationRepository.save(scheduleParticipation4);

		ScheduleParticipation  scheduleParticipation5 = ScheduleParticipation.createScheduleParticipation(schedule5, participant1, ParticipantState.PARTICIPATION_COMPLETE_APPROVAL);
		scheduleParticipationRepository.save(scheduleParticipation5);
		
        // 봉사 로그 저장(한 모집글에 20개)
        for(int i = 0; i < 20; i++){
    		Logboard logboard = Logboard.createLogBoard(content+i+i+i+i, isPublished,saveUser.getUserNo());
    		logboard.setWriter(saveUser);
    		logboard.setSchedule(createSchedule);
        	logboardList.add(logboard);
    		
    		Long logboardNo = logboardRepository.save(logboard).getLogboardNo();

			ImageParam uploadLogboardImg1 = new ImageParam(RealWorkCode.LOG, logboardNo, getMockMultipartFile(i+"_1"));
			Long saveLogboardImgId1 = imageService.addImage(uploadLogboardImg1);

			ImageParam uploadLogboardImg2 = new ImageParam(RealWorkCode.LOG, logboardNo, getMockMultipartFile(i+"_2"));
			Long saveLogboardImgId2 = imageService.addImage(uploadLogboardImg2);

			if(i%3==0){
				Like createLike = Like.createLike(RealWorkCode.LOG,logboardNo);
				createLike.setUser(saveUser);
				likeRepository.save(createLike);
				logboard.increaseLikeNum();
			}

			deleteS3ImageNoList.add(saveLogboardImgId1); // S3에 저장된 이미지 추후 삭제 예정
			deleteS3ImageNoList.add(saveLogboardImgId2); // S3에 저장된 이미지 추후 삭제 예정

			for(int j = 0 ; j < 10; j++){
				Long commentNo = replyService.addComment(saveUser, RealWorkCode.LOG, logboardNo, "comment"+j+j+j);
				if(j==0){
					replyService.deleteReply(commentNo);
				}

				for (int k =0 ; k<3; k++){
					Long replyNo = replyService.addCommentReply(saveUser, RealWorkCode.LOG, logboardNo, commentNo,"parentcomment:"+commentNo + " reply"+k+k+k);
					if(k==2){
						replyService.deleteReply(replyNo);
					}
				}
				if(logboardNo%2==0){
					replyService.addComment(saveUser, RealWorkCode.LOG, logboardNo, "comment"+j+j+j);
				}
			}
        }
		
		clear();

		// init 데이터 요약
		/*
			사용자 1명 생성
			모집글 1개 생성(staticImg)
			스케쥴 1개 생성
			로그 20개 생성
		 */
    }

    @Test
    @WithUserDetails(value = "kakao_111111", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void 로그보드_수정_조회() throws Exception {
        //when & then
        mockMvc.perform(
                get("/logboard/edit/"+logboardList.get(0).getLogboardNo()))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @WithUserDetails(value = "kakao_111111", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void 로그보드_수정_조회_없는_로그조회() throws Exception {
        //when & then
        mockMvc.perform(
                get("/logboard/edit/10000"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @WithUserDetails(value = "kakao_111111", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void 로그보드_리스트_조회() throws Exception {
        //when & then
        mockMvc.perform(
                get("/logboard?page=1&search_type=all&last_id"))
                .andExpect(status().isOk())
                .andDo(print());
    }
    
    // 봉사 로그 참여 봉사 선택
    @Test
    @WithUserDetails(value = "kakao_111111", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void 참여완료_스케줄_리스트_조회() throws Exception {
        //when & then
        mockMvc.perform(
                get("/logboard/schedule"))
                .andExpect(status().isOk())
                .andDo(print());
    }

	@Test
	@WithUserDetails(value = "kakao_111111", setupBefore = TestExecutionEvent.TEST_EXECUTION)
	void 로그보드_봉사로그및댓글_조회() throws Exception {
		//when & then
		mockMvc.perform(
						get("/logboard/"+logboardList.get(0).getLogboardNo()))
				.andExpect(status().isOk())
				.andDo(print());
	}

	@Test
	@WithUserDetails(value = "kakao_111111", setupBefore = TestExecutionEvent.TEST_EXECUTION)
	void 로그보드_봉사로그좋아요() throws Exception {
		//when & then
		mockMvc.perform(
						post("/logboard/"+logboardList.get(0).getLogboardNo()+"/like"))
				.andExpect(status().isOk())
				.andDo(print());
	}

	@Test
	@WithUserDetails(value = "kakao_111111", setupBefore = TestExecutionEvent.TEST_EXECUTION)
	void 로그보드_봉사로그좋아요_없는봉사로그번호로_실패() throws Exception {
		//when & then
		mockMvc.perform(
						post("/logboard/1000000/like"))
				.andExpect(status().isBadRequest())
				.andDo(print());
	}
}
