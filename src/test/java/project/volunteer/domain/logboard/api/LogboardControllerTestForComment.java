package project.volunteer.domain.logboard.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import project.volunteer.domain.image.application.ImageService;
import project.volunteer.domain.image.application.dto.ImageParam;
import project.volunteer.domain.image.dao.ImageRepository;
import project.volunteer.global.common.dto.CommentContentParam;
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
import project.volunteer.domain.reply.dao.ReplyRepository;
import project.volunteer.domain.reply.domain.Reply;
import project.volunteer.domain.sehedule.dao.ScheduleRepository;
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
import project.volunteer.global.common.component.ParticipantState;
import project.volunteer.global.common.component.RealWorkCode;
import project.volunteer.global.common.component.Timetable;
import project.volunteer.global.infra.s3.FileService;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class LogboardControllerTestForComment {
	@Autowired MockMvc mockMvc;
	@Autowired ObjectMapper objectMapper;
	@Autowired UserService userService;
	@Autowired UserDtoService userDtoService;
	@Autowired RecruitmentService recruitmentService;
	@Autowired ImageService imageService;
	@Autowired FileService fileService;

	@Autowired UserRepository userRepository;
	@Autowired RecruitmentRepository recruitmentRepository;
	@Autowired ImageRepository imageRepository;
	@Autowired ParticipantRepository participantRepository;
	@Autowired ScheduleRepository scheduleRepository;
	@Autowired LogboardRepository logboardRepository;
	@Autowired ReplyRepository replyRepository;

	@PersistenceContext EntityManager em;
	
	List<Logboard> logboardList= new ArrayList<>();
	Long logboardNo1; 
	Long logboardNo2;
	Long logboardNo3;
	
	private <T> String toJson(T data) throws JsonProcessingException {
		return objectMapper.writeValueAsString(data);
	}
	
	private static User saveUser;
	private static User saveUser2;
	
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
				.id("kakao_222222")
				.password("1234")
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
				.volunteerType(volunteerType).volunteerNum(volunteerNum).isIssued(isIssued).organizationName(organizationName)
				.address(address).coordinate(coordinate).timetable(timetable).isPublished(isPublished)
				.build();
		create.setWriter(saveUser);
		recruitmentRepository.save(create);
		Long no = create.getRecruitmentNo();


		// 방장 참여자 저장
		Recruitment recruitment = recruitmentRepository.findById(no).get();
		Participant participant1 = Participant.createParticipant(recruitment, saveUser, ParticipantState.JOIN_APPROVAL);
		participantRepository.save(participant1);
		

		// 스케줄 저장
		Schedule createSchedule =
				Schedule.createSchedule(timetable, content, organizationName, address, volunteerNum);
		createSchedule.setRecruitment(recruitment);
		scheduleRepository.save(createSchedule);
		
		// 봉사 로그 저장(한 모집글에 2개)
		Logboard logboard1 = Logboard.createLogBoard(content+"11111", isPublished,saveUser.getUserNo());
		logboard1.setWriter(saveUser);
		logboard1.setSchedule(createSchedule);
		logboardList.add(logboard1);
		
		Logboard logboard2 = Logboard.createLogBoard(content+"22222", isPublished,saveUser.getUserNo());
		logboard2.setWriter(saveUser);
		logboard2.setSchedule(createSchedule);
		logboardList.add(logboard2);
		
		Logboard logboard3 = Logboard.createLogBoard(content+"33333", isPublished,saveUser.getUserNo());
		logboard3.setWriter(saveUser);
		logboard3.setSchedule(createSchedule);
		logboard3.delete();
		logboardList.add(logboard3);
		
		logboardNo1 = logboardRepository.save(logboard1).getLogboardNo();
		logboardNo2 = logboardRepository.save(logboard2).getLogboardNo();
		logboardNo3 = logboardRepository.save(logboard3).getLogboardNo();
		
		clear();
	}
	
	@Test
	@WithUserDetails(value = "kakao_111111", setupBefore = TestExecutionEvent.TEST_EXECUTION)
	void 로그보드_댓글작성_성공() throws Exception {
		//when & then
		CommentContentParam param = new CommentContentParam("test comment");
		mockMvc.perform(post("/logboard/"+logboardNo1+"/comment")
						.contentType(MediaType.APPLICATION_JSON)
						.content(toJson(param)))
				.andExpect(status().isCreated())
				.andDo(print());
	}

	//facade 매퍼 클래스내에서 봉사 로그 검증 필요.
	@Disabled
	@Test
	@WithUserDetails(value = "kakao_111111", setupBefore = TestExecutionEvent.TEST_EXECUTION)
	public void vaildation체크_없는_봉사로그() throws Exception {
		//when & then
		CommentContentParam param = new CommentContentParam("write test comment");
		mockMvc.perform(post("/logboard/10000/comment")
						.contentType(MediaType.APPLICATION_JSON)
						.content(toJson(param)))
				.andExpect(status().isBadRequest())
				.andDo(print());
	}
	
	@Test
	@WithUserDetails(value = "kakao_111111", setupBefore = TestExecutionEvent.TEST_EXECUTION)
	public void validation체크_댓글내용_누락() throws Exception {
		//when & then
		CommentContentParam param = new CommentContentParam("");
		mockMvc.perform(post("/logboard/"+logboardNo1+"/comment")
						.contentType(MediaType.APPLICATION_JSON)
						.content(toJson(param)))
				.andExpect(status().isBadRequest())
				.andDo(print());
	}
	
	@Test
	@WithUserDetails(value = "kakao_111111", setupBefore = TestExecutionEvent.TEST_EXECUTION)
	void 로그보드_대댓글작성_성공() throws Exception {
		// given
		Reply reply = Reply.createComment(RealWorkCode.LOG, logboardNo1, "Test Comment");
		reply.setWriter(saveUser);
		replyRepository.save(reply);
		
		//when & then
		CommentContentParam param = new CommentContentParam("test comment");
		mockMvc.perform(post("/logboard/"+logboardNo1+"/comment/"+ reply.getReplyNo()+"/reply")
					.contentType(MediaType.APPLICATION_JSON)
					.content(toJson(param)))
			.andExpect(status().isCreated())
			.andDo(print());
	}

	//facade 매퍼 클래스내에서 봉사 로그 검증 필요.
	@Disabled
	@Test
	@WithUserDetails(value = "kakao_111111", setupBefore = TestExecutionEvent.TEST_EXECUTION)
	public void 대댓글_vaildation체크_없는_봉사로그() throws Exception {
		// given
		Reply reply = Reply.createComment(RealWorkCode.LOG, logboardNo1, "Test Comment");
		reply.setWriter(saveUser);
		replyRepository.save(reply);
		
		//when & then
		CommentContentParam param = new CommentContentParam("test comment");
		mockMvc.perform(post("/logboard/10000/comment/"+reply.getReplyNo()+"/reply")
						.contentType(MediaType.APPLICATION_JSON)
						.content(toJson(param)))
				.andExpect(status().isBadRequest())
				.andDo(print());
	}
	
	@Test
	@WithUserDetails(value = "kakao_111111", setupBefore = TestExecutionEvent.TEST_EXECUTION)
	public void validation체크_대댓글_내용_누락() throws Exception {
		// given
		Reply reply = Reply.createComment(RealWorkCode.LOG, logboardNo1, "Test Comment");
		reply.setWriter(saveUser);
		replyRepository.save(reply);
		
		//when & then
		CommentContentParam param = new CommentContentParam("");
		mockMvc.perform(post("/logboard/"+logboardNo1+"/comment/"+reply.getReplyNo()+"/reply")
						.contentType(MediaType.APPLICATION_JSON)
						.content(toJson(param)))
				.andExpect(status().isBadRequest())
				.andDo(print());
	}
	
	@Test
	@WithUserDetails(value = "kakao_111111", setupBefore = TestExecutionEvent.TEST_EXECUTION)
	public void 대대댓글_추가() throws Exception {
		// given
		Reply reply1 = Reply.createComment(RealWorkCode.LOG, logboardNo1, "Test Comment");
		reply1.setWriter(saveUser);
		replyRepository.save(reply1);
		
		Reply reply2 = Reply.createCommentReply(reply1, RealWorkCode.LOG, logboardNo1, "Test Comment Reply");
		reply2.setWriter(saveUser);
		replyRepository.save(reply2);
		
		//when & then
		CommentContentParam param = new CommentContentParam("Test Comment Reply Reply");
		mockMvc.perform(post("/logboard/"+logboardNo1+"/comment/"+reply2.getReplyNo()+"/reply")
						.contentType(MediaType.APPLICATION_JSON)
						.content(toJson(param)))
				.andExpect(status().isBadRequest())
				.andDo(print());
	}

	@Test
	@WithUserDetails(value = "kakao_111111", setupBefore = TestExecutionEvent.TEST_EXECUTION)
	public void 댓글_수정_성공() throws Exception {
		// given
		Reply reply = Reply.createComment(RealWorkCode.LOG, logboardNo1, "Test Comment");
		reply.setWriter(saveUser);
		replyRepository.save(reply);

		//when & then
		CommentContentParam  param = new CommentContentParam("Test Comment Edit");
		mockMvc.perform(put("/logboard/"+logboardNo1+"/comment/"+reply.getReplyNo())
						.contentType(MediaType.APPLICATION_JSON)
						.content(toJson(param)))
				.andExpect(status().isOk())
				.andDo(print());
	}
	
	@Test
	@WithUserDetails(value = "kakao_111111", setupBefore = TestExecutionEvent.TEST_EXECUTION)
	public void 댓글_수정_권한_없음으로_실패() throws Exception {
		// given
		Reply reply = Reply.createComment(RealWorkCode.LOG, logboardNo1, "Test Comment");
		reply.setWriter(saveUser2);
		replyRepository.save(reply);
		
		//when & then
		CommentContentParam  param = new CommentContentParam("Test Comment Edit");
		mockMvc.perform(put("/logboard/"+logboardNo1+"/comment/"+reply.getReplyNo())
						.contentType(MediaType.APPLICATION_JSON)
						.content(toJson(param)))
				.andExpect(status().isForbidden())
				.andDo(print());
	}
	

	@Test
	@WithUserDetails(value = "kakao_111111", setupBefore = TestExecutionEvent.TEST_EXECUTION)
	public void 수정하고자하는_댓글_없음() throws Exception {
		// given
		Reply reply = Reply.createComment(RealWorkCode.LOG, logboardNo1, "Test Comment");
		reply.setWriter(saveUser);
		replyRepository.save(reply);
		
		//when & then
		CommentContentParam  param = new CommentContentParam("Test Comment Edit");
		mockMvc.perform(put("/logboard/"+logboardNo1+"/comment/10000")
						.contentType(MediaType.APPLICATION_JSON)
						.content(toJson(param)))
				.andExpect(status().isBadRequest())
				.andDo(print());
	}
	

	@Test
	@WithUserDetails(value = "kakao_111111", setupBefore = TestExecutionEvent.TEST_EXECUTION)
	public void 댓글_삭제_성공() throws Exception {
		// given
		Reply reply1 = Reply.createComment(RealWorkCode.LOG, logboardNo1, "Test Comment");
		reply1.setWriter(saveUser);
		replyRepository.save(reply1);
		
		Reply reply2 = Reply.createCommentReply(reply1, RealWorkCode.LOG, logboardNo1, "Test Comment Reply");
		reply2.setWriter(saveUser);
		replyRepository.save(reply2);
		
		//when & then
		mockMvc.perform(delete("/logboard/"+logboardNo1+"/comment/"+reply2.getReplyNo()))
				.andExpect(status().isOk())
				.andDo(print());
	}

	@Test
	@WithUserDetails(value = "kakao_111111", setupBefore = TestExecutionEvent.TEST_EXECUTION)
	public void 댓글_삭제_권한_없음으로_실패() throws Exception {
		// given
		Reply reply1 = Reply.createComment(RealWorkCode.LOG, logboardNo1, "Test Comment");
		reply1.setWriter(saveUser);
		replyRepository.save(reply1);
		
		Reply reply2 = Reply.createCommentReply(reply1, RealWorkCode.LOG, logboardNo1, "Test Comment Reply");
		reply2.setWriter(saveUser2);
		replyRepository.save(reply2);
		
		//when & then
		mockMvc.perform(delete("/logboard/"+logboardNo1+"/comment/"+reply2.getReplyNo()))
				.andExpect(status().isForbidden())
				.andDo(print());
	}

	@Test
	@WithUserDetails(value = "kakao_111111", setupBefore = TestExecutionEvent.TEST_EXECUTION)
	public void 삭제하고자하는_댓글_없음() throws Exception {
		// given
		Reply reply1 = Reply.createComment(RealWorkCode.LOG, logboardNo1, "Test Comment");
		reply1.setWriter(saveUser);
		replyRepository.save(reply1);
		
		Reply reply2 = Reply.createCommentReply(reply1, RealWorkCode.LOG, logboardNo1, "Test Comment Reply");
		reply2.setWriter(saveUser2);
		replyRepository.save(reply2);
		
		//when & then
		mockMvc.perform(delete("/logboard/"+logboardNo1+"/comment/10000"))
				.andExpect(status().isBadRequest())
				.andDo(print());
	}
	
}
