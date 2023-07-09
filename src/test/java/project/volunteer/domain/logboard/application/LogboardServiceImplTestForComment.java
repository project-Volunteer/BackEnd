package project.volunteer.domain.logboard.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import project.volunteer.domain.image.application.ImageService;
import project.volunteer.domain.image.application.dto.ImageParam;
import project.volunteer.domain.image.dao.ImageRepository;
import project.volunteer.domain.image.domain.ImageType;
import project.volunteer.domain.logboard.api.dto.request.AddLogboardCommentParam;
import project.volunteer.domain.logboard.api.dto.request.AddLogboardCommentReplyParam;
import project.volunteer.domain.logboard.api.dto.request.DeleteLogboardReplyParam;
import project.volunteer.domain.logboard.api.dto.request.EditLogboardReplyParam;
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
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;
import project.volunteer.global.infra.s3.FileService;

@SpringBootTest
@Transactional
public class LogboardServiceImplTestForComment {
	@Autowired UserRepository userRepository;
	@Autowired ParticipantRepository participantRepository;
	@Autowired RecruitmentRepository recruitmentRepository;
	@Autowired ImageRepository imageRepository;
	@Autowired RecruitmentService recruitmentService;
	@Autowired ImageService imageService;
	@Autowired FileService fileService;
	@Autowired UserService userService;
	@Autowired ScheduleRepository scheduleRepository;
	@Autowired UserDtoService userDtoService;
	@Autowired LogboardRepository logboardRepository;
	@Autowired LogboardService logboardService;
	@Autowired ReplyRepository replyRepository;
	@PersistenceContext EntityManager em;
	
	List<Logboard> logboardList= new ArrayList<>();
	Long logboardNo1; 
	Long logboardNo2;
	Long logboardNo3;
	
	
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
		Address address = new Address("11", "110011", details);
		int volunteerNum = 10;
		
		Recruitment create = Recruitment.builder()
				.title(title).content(content).volunteeringCategory(category).volunteeringType(volunteeringType)
				.volunteerType(volunteerType).volunteerNum(volunteerNum).isIssued(isIssued).organizationName(organizationName)
				.address(address).coordinate(coordinate).timetable(timetable).isPublished(isPublished)
				.build();
		create.setWriter(saveUser);
		recruitmentRepository.save(create);
		Long no = create.getRecruitmentNo();

		// static 이미지 저장
		ImageParam staticImageDto1 = ImageParam.builder().code(RealWorkCode.RECRUITMENT).imageType(ImageType.STATIC)
				.no(no).staticImageCode("imgname1").uploadImage(null).build();
		imageService.addImage(staticImageDto1);	

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
	void 로그보드_댓글작성_성공() throws Exception {
		// given 
		AddLogboardCommentParam  param = new AddLogboardCommentParam(logboardNo1, "test comment");
		
		// when
		Long commentNo = logboardService.addLogComment(param, saveUser.getUserNo());
		
		// then
		Reply createReply = replyRepository.findReply(RealWorkCode.LOG, logboardNo1, commentNo).get();

		Assertions.assertThat(createReply.getContent().equals("test comment"));
		Assertions.assertThat(createReply.getCreateUserNo().equals(saveUser.getUserNo()));
		Assertions.assertThat(!createReply.getCreatedDate().toString().isEmpty());
	}

	@Test
	public void 댓글작성_없는_사용자_요청으로_실패() throws Exception {
		// given 
		AddLogboardCommentParam  param = new AddLogboardCommentParam(logboardNo1, "test comment");
		
        //when & then
        assertThatThrownBy(() -> logboardService.addLogComment(param, 10000L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.NOT_EXIST_USER.name());
	}
	
	@Test
	public void 댓글작성_없는_로그번호_요청으로_실패() throws Exception {
		// given 
		AddLogboardCommentParam  param = new AddLogboardCommentParam(1000L, "test comment");
		
        //when & then
        assertThatThrownBy(() -> logboardService.addLogComment(param, saveUser.getUserNo()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.NOT_EXIST_LOGBOARD.name());
	}
	
	@Test
	void 로그보드_대댓글작성_성공() throws Exception {
		// given 
		Reply createComment = Reply.createComment(RealWorkCode.LOG, logboardNo1, "Test Comment Reply", saveUser.getUserNo());
		createComment.setWriter(saveUser);
		replyRepository.save(createComment);
		
		AddLogboardCommentReplyParam  param = new AddLogboardCommentReplyParam(logboardNo1, createComment.getReplyNo(), "test comment");
		
		// when
		Long commentReplyNo = logboardService.addLogCommentReply(param, saveUser.getUserNo());
		
		// then
		Reply createCommentReply = replyRepository.findReply(RealWorkCode.LOG, logboardNo1, commentReplyNo).get();

		Assertions.assertThat(createCommentReply.getContent().equals("Test Comment Reply"));
		Assertions.assertThat(createCommentReply.getCreateUserNo().equals(saveUser.getUserNo()));
		Assertions.assertThat(!createCommentReply.getCreatedDate().toString().isEmpty());
	}

	@Test
	public void 대댓글_부모댓글이_없어_실패() throws Exception {
		// given 
		AddLogboardCommentReplyParam  param = new AddLogboardCommentReplyParam(logboardNo1, 10000L, "test comment");
		
		// when & then
        assertThatThrownBy(() -> logboardService.addLogCommentReply(param, saveUser.getUserNo()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.NOT_EXIST_PARENT_REPLY.name());
	}
	
	@Test
	public void 대대댓글_추가() throws Exception {
		// given
		Reply createComment = Reply.createComment(RealWorkCode.LOG, logboardNo1, "Test Comment", saveUser.getUserNo());
		createComment.setWriter(saveUser);
		replyRepository.save(createComment);
		
		Reply createCommentReply = Reply.createCommentReply(createComment, RealWorkCode.LOG, logboardNo1, "Test Comment Reply", saveUser.getUserNo());
		createCommentReply.setWriter(saveUser);
		replyRepository.save(createCommentReply);
		
		//when & then
		AddLogboardCommentReplyParam  param = new AddLogboardCommentReplyParam(logboardNo1, createCommentReply.getReplyNo(), "Test Comment Reply Reply");

        assertThatThrownBy(() -> logboardService.addLogCommentReply(param, saveUser.getUserNo()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.ALREADY_HAS_PARENT_REPLY.name());
	}

	@Test
	public void 댓글_수정_성공() throws Exception {
		// given
		Reply createComment = Reply.createComment(RealWorkCode.LOG, logboardNo1, "Test Comment", saveUser.getUserNo());
		createComment.setWriter(saveUser);
		replyRepository.save(createComment);
		
		//when
		EditLogboardReplyParam param = new EditLogboardReplyParam(createComment.getReplyNo(), "Test Comment Edit");
		logboardService.editLogReply(param, saveUser.getUserNo());
		
		// then
		Reply modifyComment = replyRepository.findReply(RealWorkCode.LOG, logboardNo1, param.getReplyNo()).get();
		
		Assertions.assertThat(modifyComment.getContent().equals("Test Comment Edit"));
		Assertions.assertThat(modifyComment.getModifiedUserNo().equals(saveUser.getUserNo()));
		Assertions.assertThat(!modifyComment.getModifiedDate().toString().isEmpty());
	}
	
	@Test
	public void 댓글_수정_권한_없음으로_실패() throws Exception {
		// given
		Reply reply = Reply.createComment(RealWorkCode.LOG, logboardNo1, "Test Comment", saveUser2.getUserNo());
		reply.setWriter(saveUser2);
		replyRepository.save(reply);
		
		//when & then
		EditLogboardReplyParam  param = new EditLogboardReplyParam(reply.getReplyNo(), "Test Comment Edit");

        assertThatThrownBy(() -> logboardService.editLogReply(param, saveUser.getUserNo()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.FORBIDDEN_REPLY.name());
	}
	

	@Test
	public void 수정하고자하는_댓글_없음으로_실패() throws Exception {
		// given
		Reply reply = Reply.createComment(RealWorkCode.LOG, logboardNo1, "Test Comment", saveUser.getUserNo());
		reply.setWriter(saveUser);
		replyRepository.save(reply);
		
		//when & then
		EditLogboardReplyParam  param = new EditLogboardReplyParam(10000L, "Test Comment Edit");
		
        assertThatThrownBy(() -> logboardService.editLogReply(param, saveUser.getUserNo()))
		        .isInstanceOf(BusinessException.class)
		        .hasMessageContaining(ErrorCode.NOT_EXIST_REPLY.name());
	}
	

	@Test
	public void 댓글_삭제_성공() throws Exception {
		// given
		Reply reply1 = Reply.createComment(RealWorkCode.LOG, logboardNo1, "Test Comment", saveUser.getUserNo());
		reply1.setWriter(saveUser);
		replyRepository.save(reply1);
		
		Reply reply2 = Reply.createCommentReply(reply1, RealWorkCode.LOG, logboardNo1, "Test Comment Reply", saveUser.getUserNo());
		reply2.setWriter(saveUser);
		replyRepository.save(reply2);
		
		//when
		List<Reply> existsComment = replyRepository.findReplyList(RealWorkCode.LOG, logboardNo1);
		
		DeleteLogboardReplyParam  param = new DeleteLogboardReplyParam(reply2.getReplyNo());
		logboardService.deleteLogReply(param, saveUser.getUserNo());

		List<Reply> afterDeleteComment = replyRepository.findReplyList(RealWorkCode.LOG, logboardNo1);
		
		// then
		Assertions.assertThat(existsComment.size() == afterDeleteComment.size()+1);
	}

	@Test
	public void 댓글_삭제_권한_없음으로_실패() throws Exception {
		// given
		Reply reply1 = Reply.createComment(RealWorkCode.LOG, logboardNo1, "Test Comment", saveUser.getUserNo());
		reply1.setWriter(saveUser);
		replyRepository.save(reply1);
		
		Reply reply2 = Reply.createCommentReply(reply1, RealWorkCode.LOG, logboardNo1, "Test Comment Reply", saveUser2.getUserNo());
		reply2.setWriter(saveUser2);
		replyRepository.save(reply2);
		
		//when & then
		DeleteLogboardReplyParam  param = new DeleteLogboardReplyParam(reply2.getReplyNo());

        assertThatThrownBy(() -> logboardService.deleteLogReply(param, saveUser.getUserNo()))
		        .isInstanceOf(BusinessException.class)
		        .hasMessageContaining(ErrorCode.FORBIDDEN_REPLY.name());
	}

	@Test
	public void 삭제하고자하는_댓글_없음() throws Exception {
		// given
		Reply reply1 = Reply.createComment(RealWorkCode.LOG, logboardNo1, "Test Comment", saveUser.getUserNo());
		reply1.setWriter(saveUser);
		replyRepository.save(reply1);
		
		Reply reply2 = Reply.createCommentReply(reply1, RealWorkCode.LOG, logboardNo1, "Test Comment Reply", saveUser2.getUserNo());
		reply2.setWriter(saveUser2);
		replyRepository.save(reply2);
		
		//when & then
		DeleteLogboardReplyParam  param = new DeleteLogboardReplyParam(1000L);

        assertThatThrownBy(() -> logboardService.deleteLogReply(param, saveUser.getUserNo()))
		        .isInstanceOf(BusinessException.class)
		        .hasMessageContaining(ErrorCode.NOT_EXIST_REPLY.name());
	}
	
	
}
