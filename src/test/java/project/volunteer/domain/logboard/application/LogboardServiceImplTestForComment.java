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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import project.volunteer.domain.image.application.ImageService;
import project.volunteer.domain.image.dao.ImageRepository;
import project.volunteer.domain.recruitmentParticipation.domain.RecruitmentParticipation;
import project.volunteer.global.common.component.IsDeleted;
import project.volunteer.global.common.dto.CommentContentParam;
import project.volunteer.domain.logboard.dao.LogboardRepository;
import project.volunteer.domain.logboard.domain.Logboard;
import project.volunteer.domain.recruitmentParticipation.repository.RecruitmentParticipationRepository;
import project.volunteer.domain.recruitment.application.RecruitmentCommandUseCase;
import project.volunteer.domain.recruitment.repository.RecruitmentRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.reply.application.ReplyService;
import project.volunteer.domain.reply.dao.ReplyRepository;
import project.volunteer.domain.reply.domain.Reply;
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
import project.volunteer.global.common.component.ParticipantState;
import project.volunteer.global.common.component.RealWorkCode;
import project.volunteer.global.common.component.Timetable;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;
import project.volunteer.global.infra.s3.FileService;

@SpringBootTest
@Transactional
public class LogboardServiceImplTestForComment {
	@Autowired UserService userService;
	@Autowired UserDtoService userDtoService;
	@Autowired
	RecruitmentCommandUseCase recruitmentService;
	@Autowired FileService fileService;
	@Autowired ImageService imageService;
	@Autowired LogboardService logboardService;
	@Autowired ReplyService replyService;

	@Autowired UserRepository userRepository;
	@Autowired RecruitmentRepository recruitmentRepository;
	@Autowired ImageRepository imageRepository;
	@Autowired
    RecruitmentParticipationRepository participantRepository;
	@Autowired ScheduleRepository scheduleRepository;
	@Autowired LogboardRepository logboardRepository;
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
		RecruitmentParticipation participant1 = RecruitmentParticipation.createParticipant(recruitment, saveUser, ParticipantState.JOIN_APPROVAL);
		participantRepository.save(participant1);
		

		// 스케줄 저장
		Schedule createSchedule =
				Schedule.create(recruitment, timetable, content, organizationName, address, volunteerNum);
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
		CommentContentParam param = new CommentContentParam("test comment");
		
		// when
		Long commentNo = replyService.addComment(saveUser, RealWorkCode.LOG, logboardNo1, param.getContent());
		
		// then
		Reply createReply = replyRepository.findReply(RealWorkCode.LOG, logboardNo1, commentNo).get();

		Assertions.assertThat(createReply.getContent().equals("test comment"));
		Assertions.assertThat(!createReply.getCreatedDate().toString().isEmpty());
	}

	//댓글 Service 테스트 내용
	@Disabled
	@Test
	public void 댓글작성_없는_사용자_요청으로_실패() throws Exception {
		// given 
		CommentContentParam param = new CommentContentParam("test comment");
		
        //when & then
        assertThatThrownBy(() -> replyService.addComment(null, RealWorkCode.LOG, logboardNo1, param.getContent()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.NOT_EXIST_USER.name());
	}

	//로그 보드 Service 테스트 내용
	@Disabled
	@Test
	public void 댓글작성_없는_로그번호_요청으로_실패() throws Exception {
		// given 
		CommentContentParam param = new CommentContentParam("test comment");
		
        //when & then
        assertThatThrownBy(() -> replyService.addComment(saveUser, RealWorkCode.LOG, 100000L, param.getContent()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.NOT_EXIST_LOGBOARD.name());
	}
	
	@Test
	void 로그보드_대댓글작성_성공() throws Exception {
		// given 
		Reply createComment = Reply.createComment(RealWorkCode.LOG, logboardNo1, "Test Comment Reply");
		createComment.setWriter(saveUser);
		replyRepository.save(createComment);

		CommentContentParam param = new CommentContentParam("test comment");
		
		// when
		Long commentReplyNo = replyService.addCommentReply(saveUser, RealWorkCode.LOG, logboardNo1, createComment.getReplyNo(), param.getContent());
		
		// then
		Reply createCommentReply = replyRepository.findReply(RealWorkCode.LOG, logboardNo1, commentReplyNo).get();

		Assertions.assertThat(createCommentReply.getContent().equals("Test Comment Reply"));
		Assertions.assertThat(!createCommentReply.getCreatedDate().toString().isEmpty());
	}

	@Test
	public void 대댓글_부모댓글이_없어_실패() throws Exception {
		// given 
		CommentContentParam param = new CommentContentParam("test comment");
		
		// when & then
        assertThatThrownBy(() -> replyService.addCommentReply(saveUser, RealWorkCode.LOG, logboardNo1, 10000L, param.getContent()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.NOT_EXIST_PARENT_REPLY.name());
	}
	
	@Test
	public void 대대댓글_추가() throws Exception {
		// given
		Reply createComment = Reply.createComment(RealWorkCode.LOG, logboardNo1, "Test Comment");
		createComment.setWriter(saveUser);
		replyRepository.save(createComment);
		
		Reply createCommentReply = Reply.createCommentReply(createComment, RealWorkCode.LOG, logboardNo1, "Test Comment Reply");
		createCommentReply.setWriter(saveUser);
		replyRepository.save(createCommentReply);
		
		//when & then
		CommentContentParam param = new CommentContentParam( "Test Comment Reply Reply");

        assertThatThrownBy(() -> replyService.addCommentReply(saveUser, RealWorkCode.LOG, logboardNo1, createCommentReply.getReplyNo(), param.getContent()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.ALREADY_HAS_PARENT_REPLY.name());
	}

	@Test
	public void 댓글_수정_성공() throws Exception {
		// given
		Reply createComment = Reply.createComment(RealWorkCode.LOG, logboardNo1, "Test Comment");
		createComment.setWriter(saveUser);
		replyRepository.save(createComment);
		
		//when
		CommentContentParam param = new CommentContentParam("Test Comment Edit");
		replyService.editReply(createComment.getReplyNo(), param.getContent());
		
		// then
		Reply modifyComment = replyRepository.findReply(RealWorkCode.LOG, logboardNo1, createComment.getReplyNo()).get();
		
		Assertions.assertThat(modifyComment.getContent().equals("Test Comment Edit"));
		Assertions.assertThat(!modifyComment.getModifiedDate().toString().isEmpty());
	}
	
	@Test
	@Disabled(value = "인터셉터로 권한 검증 분리됨.")
	public void 댓글_수정_권한_없음으로_실패() throws Exception {
		// given
		Reply reply = Reply.createComment(RealWorkCode.LOG, logboardNo1, "Test Comment");
		reply.setWriter(saveUser2);
		replyRepository.save(reply);
		
		//when & then
		CommentContentParam  param = new CommentContentParam("Test Comment Edit");

        assertThatThrownBy(() -> replyService.editReply(reply.getReplyNo(), param.getContent()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.FORBIDDEN_REPLY.name());
	}
	

	@Test
	public void 수정하고자하는_댓글_없음으로_실패() throws Exception {
		// given
		Reply reply = Reply.createComment(RealWorkCode.LOG, logboardNo1, "Test Comment");
		reply.setWriter(saveUser);
		replyRepository.save(reply);
		
		//when & then
		CommentContentParam  param = new CommentContentParam("Test Comment Edit");

		assertThatThrownBy(() -> replyService.editReply(10000L, param.getContent()))
		        .isInstanceOf(BusinessException.class)
		        .hasMessageContaining(ErrorCode.NOT_EXIST_REPLY.name());
	}
	

	@Test
	public void 댓글_삭제_성공() throws Exception {
		// given
		Reply reply1 = Reply.createComment(RealWorkCode.LOG, logboardNo1, "Test Comment");
		reply1.setWriter(saveUser);
		replyRepository.save(reply1);
		
		Reply reply2 = Reply.createCommentReply(reply1, RealWorkCode.LOG, logboardNo1, "Test Comment Reply");
		reply2.setWriter(saveUser);
		replyRepository.save(reply2);
		
		//when
		List<Reply> existsComment = replyRepository.findReplyList(RealWorkCode.LOG, logboardNo1);
		
		replyService.deleteReply(reply2.getReplyNo());

		List<Reply> afterDeleteComment = replyRepository.findReplyList(RealWorkCode.LOG, logboardNo1);
		
		// then
		Assertions.assertThat(existsComment.size() == afterDeleteComment.size()+1);
	}

	@Test
	public void 삭제하고자하는_댓글_없음() throws Exception {
		// given
		Reply reply1 = Reply.createComment(RealWorkCode.LOG, logboardNo1, "Test Comment");
		reply1.setWriter(saveUser);
		replyRepository.save(reply1);
		
		Reply reply2 = Reply.createCommentReply(reply1, RealWorkCode.LOG, logboardNo1, "Test Comment Reply");
		reply2.setWriter(saveUser2);
		replyRepository.save(reply2);
		
		//when & then
		assertThatThrownBy(() -> replyService.deleteReply(10000L))
		        .isInstanceOf(BusinessException.class)
		        .hasMessageContaining(ErrorCode.NOT_EXIST_REPLY.name());
	}
	
	
}
