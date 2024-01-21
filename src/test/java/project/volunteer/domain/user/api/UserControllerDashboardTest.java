package project.volunteer.domain.user.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.logboard.application.LogboardService;
import project.volunteer.domain.participation.dao.ParticipantRepository;
import project.volunteer.domain.participation.domain.Participant;
import project.volunteer.domain.recruitment.application.RecruitmentService;
import project.volunteer.domain.recruitment.application.dto.RecruitmentParam;
import project.volunteer.domain.recruitment.dao.RecruitmentRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.scheduleParticipation.dao.ScheduleParticipationRepository;
import project.volunteer.domain.scheduleParticipation.domain.ScheduleParticipation;
import project.volunteer.domain.sehedule.application.ScheduleCommandUseCase;
import project.volunteer.domain.sehedule.application.dto.ScheduleUpsertCommand;
import project.volunteer.domain.sehedule.dao.ScheduleRepository;
import project.volunteer.domain.user.api.dto.request.LogboardListRequestParam;
import project.volunteer.domain.user.api.dto.request.RecruitmentListRequestParam;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.domain.Gender;
import project.volunteer.domain.user.domain.Role;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.*;
import project.volunteer.restdocs.document.config.RestDocsConfiguration;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import(RestDocsConfiguration.class)
public class UserControllerDashboardTest {
    @PersistenceContext EntityManager em;
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired ParticipantRepository participantRepository;
    @Autowired RecruitmentService recruitmentService;
    @Autowired RecruitmentRepository recruitmentRepository;
    @Autowired
    ScheduleCommandUseCase scheduleService;
    @Autowired ScheduleRepository scheduleRepository;
    @Autowired ScheduleParticipationRepository spRepository;
    @Autowired LogboardService logboardService;
    @Autowired RestDocumentationResultHandler restDocs;

    private static User user1;
    private static User user2;
    private static List<Long> deleteRecruitmentNoList = new ArrayList<>();
    private static List<Long> deleteLogboardNoList = new ArrayList<>();

    final String AUTHORIZATION_HEADER = "accessToken";

    private void clear() {
        em.flush();
        em.clear();
    }

    @BeforeEach
    public void init(){
		/* init 데이터 요약
			사용자 2명 생성
			모집글 7개 생성
			참여글 8개 생성
			로그 6개 생성

			모집글 상세
			모집글 1, 2 - 사용자1이 저장
			모집글 3 - 사용자1이 임시저장
			모집글 4, 5, 6, 7 - 사용자2가 만듦

			봉사 모집 참여
			모집글 1, 2 - 작성자 본인(사용자1) 참여 완료
			모집글 4, 5 - 사용자1이 참여 요청
			모집글 6, 7 - 사용자1이 승인 완료
			모집글 1 - 사용자2가 승인 완료
			모집글 2 - 사용자2가 참여 요청

			참여글 상세
			모집글 1 - 스케줄 1, 2, 3 - 봉사시간 3시간
			모집글 5 - 스케줄 4, 5, 6 - 봉사시간 2시간
			모집글 6 - 스케줄 7, 8 - 봉사시간 1시간씩 총 2시간

			일정 잠여
			모집글 1 - 스케줄 1, 2, 3 - 사용자1 참여 완료
			모집글 1 - 스케줄 1 - 사용자2 참여 완료
			모집글 1 - 스케줄 1 - 사용자2 참여 완료 대기
			모집글 5 - 스케줄 4, 5, 6 - 사용자1 참여 완료
			모집글 6 - 스케줄 7, 8 - 사용자1 참여완료 대기

            로그 상세
			모집글 1 - 스케줄 1, 2, 3 - 사용자 1이 log 작성
			모집글 1 - 스케줄 1 - 사용자 2가 log 임시저장
			모집글 5 - 스케줄 4, 5, 6 - 사용자 1이 log 임시 저장
		*/

        user1 = userRepository.save(User.builder()
                .id("kakao_111111").password("1234").nickName("nickname11").email("email11@gmail.com").gender(Gender.M)
                .birthDay(LocalDate.now()).picture("picture1111").joinAlarmYn(true).beforeAlarmYn(true).noticeAlarmYn(true)
                .role(Role.USER).provider("kakao").providerId("111111")
                .build());

        user2 = userRepository.save(User.builder()
                .id("kakao_222222").password("1234").nickName("nickname22").email("email22@gmail.com").gender(Gender.W)
                .birthDay(LocalDate.now()).picture("picture2222").joinAlarmYn(true).beforeAlarmYn(true).noticeAlarmYn(true)
                .role(Role.USER).provider("kakao").providerId("222222")
                .build());


        /*  모집글 상세
			모집글 1, 2 - 사용자1이 저장
			모집글 3 - 사용자1이 임시저장
			모집글 4, 5, 6, 7 - 사용자2가 만듦
			모집글 8, 9, 10, 11 - 사용자2가 임시저장
			*/
        Recruitment rNo1 = recruitmentService.addRecruitment(user1, makeRecruitmentParam(1, 3 , true ));
        Recruitment rNo2 = recruitmentService.addRecruitment(user1, makeRecruitmentParam(2, 3 , true ));
        Recruitment rNo3 = recruitmentService.addRecruitment(user1, makeRecruitmentParam(3, 3 , false ));
        Recruitment rNo4 = recruitmentService.addRecruitment(user2, makeRecruitmentParam(4, 2 , true ));
        Recruitment rNo5 = recruitmentService.addRecruitment(user2, makeRecruitmentParam(5, 2 , true ));
        Recruitment rNo6 = recruitmentService.addRecruitment(user2, makeRecruitmentParam(6, 2 , true ));
        Recruitment rNo7 = recruitmentService.addRecruitment(user2, makeRecruitmentParam(7, 1 , true ));

        Recruitment rNo8 = recruitmentService.addRecruitment(user2, makeRecruitmentParam(8, 3 , false ));
        Recruitment rNo9 = recruitmentService.addRecruitment(user2, makeRecruitmentParam(9, 3 , false ));
        Recruitment rNo10 = recruitmentService.addRecruitment(user2, makeRecruitmentParam(0, 3 , false ));
        Recruitment rNo11 = recruitmentService.addRecruitment(user2, makeRecruitmentParam(1, 3 , false ));

        deleteRecruitmentNoList.add(rNo9.getRecruitmentNo());
        deleteRecruitmentNoList.add(rNo10.getRecruitmentNo());
        deleteRecruitmentNoList.add(rNo11.getRecruitmentNo());



        /*  봉사 모집 참여
			모집글 1, 2 - 작성자 본인(사용자1) 참여 완료
			모집글 4, 5 - 사용자1이 참여 요청
			모집글 6, 7 - 사용자1이 승인 완료
			모집글 1 - 사용자2가 승인 완료
			모집글 2 - 사용자2가 참여 요청 */
        Participant r1p1 = participantRepository.save(Participant.builder()
                .participant(user1).state(ParticipantState.JOIN_APPROVAL).recruitment(recruitmentRepository.findById(rNo1.getRecruitmentNo()).get())
                .build());
        Participant r2p2 = participantRepository.save(Participant.builder()
                .participant(user1).state(ParticipantState.JOIN_APPROVAL).recruitment(recruitmentRepository.findById(rNo2.getRecruitmentNo()).get())
                .build());
        Participant r4p3 = participantRepository.save(Participant.builder()
                .participant(user1).state(ParticipantState.JOIN_REQUEST).recruitment(recruitmentRepository.findById(rNo4.getRecruitmentNo()).get())
                .build());
        Participant r5p4 =  participantRepository.save(Participant.builder()
                .participant(user1).state(ParticipantState.JOIN_REQUEST).recruitment(recruitmentRepository.findById(rNo5.getRecruitmentNo()).get())
                .build());
        Participant r6p5 = participantRepository.save(Participant.builder()
                .participant(user1).state(ParticipantState.JOIN_APPROVAL).recruitment(recruitmentRepository.findById(rNo6.getRecruitmentNo()).get())
                .build());
        Participant r7p6 = participantRepository.save(Participant.builder()
                .participant(user1).state(ParticipantState.JOIN_APPROVAL).recruitment(recruitmentRepository.findById(rNo7.getRecruitmentNo()).get())
                .build());
        Participant r1p7 = participantRepository.save(Participant.builder()
                .participant(user2).state(ParticipantState.JOIN_APPROVAL).recruitment(recruitmentRepository.findById(rNo1.getRecruitmentNo()).get())
                .build());
        Participant r2p8 = participantRepository.save(Participant.builder()
                .participant(user2).state(ParticipantState.JOIN_REQUEST).recruitment(recruitmentRepository.findById(rNo2.getRecruitmentNo()).get())
                .build());


        /*  참여글 상세
            모집글 1 - 스케줄 1, 2, 3 - 봉사시간 3,2,3 시간
            모집글 6 - 스케줄 4, 5, 6 - 봉사시간 2,3,2시간
            모집글 7 - 스케줄 7, 8 - 봉사시간 1시간씩 총 2시간 */
        Long sNo1 = scheduleService.addSchedule(rNo1, makeScheduleParam(1, 3));
        Long sNo2 = scheduleService.addSchedule(rNo1, makeScheduleParam(2, 2));
        Long sNo3 = scheduleService.addSchedule(rNo1, makeScheduleParam(3, 3));

        Long sNo4 = scheduleService.addSchedule(rNo6, makeScheduleParam(4, 2));
        Long sNo5 = scheduleService.addSchedule(rNo6, makeScheduleParam(5, 3));
        Long sNo6 = scheduleService.addSchedule(rNo6, makeScheduleParam(6, 2));

        Long sNo7 = scheduleService.addSchedule(rNo7, makeScheduleParam(7, 1));
        Long sNo8 = scheduleService.addSchedule(rNo7, makeScheduleParam(8, 1));


        /*  일정 잠여
			모집글 1 - 스케줄 1, 2, 3 - 사용자1 참여 완료
			모집글 1 - 스케줄 1 - 사용자2 참여 완료
			모집글 1 - 스케줄 2 - 사용자2 참여 완료 대기
			모집글 6 - 스케줄 4, 5, 6 - 사용자1 참여 완료
			모집글 6 - 스케줄 4, 5, 6 - 사용자2 일정 참여중
			모집글 7 - 스케줄 7, 8 - 사용자1 참여완료 대기 */
        spRepository.save(ScheduleParticipation.createScheduleParticipation(
            scheduleRepository.findById(sNo1).get(), r1p1, ParticipantState.PARTICIPATION_COMPLETE_APPROVAL));
        spRepository.save(ScheduleParticipation.createScheduleParticipation(
            scheduleRepository.findById(sNo2).get(), r1p1, ParticipantState.PARTICIPATION_COMPLETE_APPROVAL));
        spRepository.save(ScheduleParticipation.createScheduleParticipation(
            scheduleRepository.findById(sNo3).get(), r1p1, ParticipantState.PARTICIPATION_COMPLETE_APPROVAL));

        spRepository.save(ScheduleParticipation.createScheduleParticipation(
            scheduleRepository.findById(sNo4).get(), r6p5, ParticipantState.PARTICIPATION_COMPLETE_APPROVAL));
        spRepository.save(ScheduleParticipation.createScheduleParticipation(
            scheduleRepository.findById(sNo5).get(), r6p5, ParticipantState.PARTICIPATION_COMPLETE_APPROVAL));
        spRepository.save(ScheduleParticipation.createScheduleParticipation(
            scheduleRepository.findById(sNo6).get(), r6p5, ParticipantState.PARTICIPATION_COMPLETE_APPROVAL));

        spRepository.save(ScheduleParticipation.createScheduleParticipation(
            scheduleRepository.findById(sNo7).get(), r7p6, ParticipantState.PARTICIPATION_COMPLETE_UNAPPROVED));
        spRepository.save(ScheduleParticipation.createScheduleParticipation(
            scheduleRepository.findById(sNo8).get(), r7p6, ParticipantState.PARTICIPATION_COMPLETE_UNAPPROVED));

        spRepository.save(ScheduleParticipation.createScheduleParticipation(
                scheduleRepository.findById(sNo1).get(), r1p7, ParticipantState.PARTICIPATION_COMPLETE_APPROVAL));
        spRepository.save(ScheduleParticipation.createScheduleParticipation(
                scheduleRepository.findById(sNo2).get(), r1p7, ParticipantState.PARTICIPATION_COMPLETE_UNAPPROVED));

        spRepository.save(ScheduleParticipation.createScheduleParticipation(
                scheduleRepository.findById(sNo4).get(), r1p7, ParticipantState.PARTICIPATING));
        spRepository.save(ScheduleParticipation.createScheduleParticipation(
                scheduleRepository.findById(sNo5).get(), r1p7, ParticipantState.PARTICIPATING));
        spRepository.save(ScheduleParticipation.createScheduleParticipation(
                scheduleRepository.findById(sNo6).get(), r1p7, ParticipantState.PARTICIPATING));


        /*  로그 상세
            모집글 1 - 스케줄 1, 2, 3 - 사용자 1이 log 작성
			모집글 1 - 스케줄 1 - 사용자 2가 log 임시저장
            모집글 5 - 스케줄 4, 5, 6 - 사용자 1이 log 임시 저장  */
        logboardService.addLog(user1.getUserNo(), "test contents1", sNo1, true);
        logboardService.addLog(user1.getUserNo(), "test contents2", sNo2, true);
        logboardService.addLog(user1.getUserNo(), "test contents3", sNo3, true);

        Long log1= logboardService.addLog(user1.getUserNo(), "test contents4", sNo4, false);
        Long log2= logboardService.addLog(user1.getUserNo(), "test contents5", sNo5, false);
        Long log3= logboardService.addLog(user1.getUserNo(), "test contents6", sNo6, false);

        deleteLogboardNoList.add(log1);
        deleteLogboardNoList.add(log2);
        deleteLogboardNoList.add(log3);


        logboardService.addLog(user2.getUserNo(), "test contents5", sNo1, false);

        clear();
    }

    @Test
    @Transactional
    @DisplayName("user1의 마이페이지 대시보드 조회")
    @WithUserDetails(value = "kakao_111111", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void mypageInfo() throws Exception {
        // when & then
        ResultActions result = mockMvc.perform(RestDocumentationRequestBuilders.get("/user/info")
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
                                        fieldWithPath("userInfo.nickName").type(JsonFieldType.STRING).description("로그인 사용자의 닉네임"),
                                        fieldWithPath("userInfo.email").type(JsonFieldType.STRING).description("로그인 사용자의 이메일"),
                                        fieldWithPath("userInfo.profile").type(JsonFieldType.STRING).description("로그인 사용자의 프로필 사진 URL"),

                                        fieldWithPath("historyTimeInfo.totalTime").type(JsonFieldType.NUMBER).description("봉사 일정 참여 한 총 시간"),
                                        fieldWithPath("historyTimeInfo.totalCnt").type(JsonFieldType.NUMBER).description("봉사 일정 참여 한 총 횟수"),


                                        fieldWithPath("activityInfo.joinApprovalCnt").type(JsonFieldType.NUMBER).description("내가 참여중인 봉사 모집글 수"),
                                        fieldWithPath("activityInfo.joinRequestCnt").type(JsonFieldType.NUMBER).description("내가 참여 요청한 봉사 모집글 수"),
                                        fieldWithPath("activityInfo.recruitingCnt").type(JsonFieldType.NUMBER).description("내가 모집중인 봉사 모집글 수"),
                                        fieldWithPath("activityInfo.tempSavingCnt").type(JsonFieldType.NUMBER).description("내가 임시저장한(log, recruitment) 글 수")
                                )
                        )
                );

        /*
            {
                "userInfo":{"nicName":"nickname11","email":"email11@gmail.com","profile":"picture1111"}
                ,"historyTimeInfo":{"totalTime":15,"totalCnt":6}
                ,"activityInfo":{"joinApprovalCnt":4,"joinRequestCnt":2,"recruitingCnt":2,"tempSavingCnt":4}
            }
        */

    }
    @Disabled
    @Test
    @DisplayName("user2의 마이페이지 대시보드 조회")
    @WithUserDetails(value = "kakao_222222", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void myPage_dashboard2() throws Exception {
        // when & then
        mockMvc.perform(
                        get("/user/info"))
                .andExpect(status().isOk())
                .andDo(print());
    }


    @Test
    @Transactional
    @DisplayName("user1의 마이페이지 봉사이력 조회")
    @WithUserDetails(value = "kakao_111111", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void mypageHistory() throws Exception {
        // when & then
        ResultActions result = mockMvc.perform(RestDocumentationRequestBuilders.get("/user/history?page=1")
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
                                        fieldWithPath("isLast").type(JsonFieldType.BOOLEAN).description("마지막 봉사 참여 이력 유무"),
                                        fieldWithPath("lastId").type(JsonFieldType.NUMBER).description("응답 봉사 참여 이력 리스트 중 마지막 이력 고유키 PK"),
                                        fieldWithPath("histories").type(JsonFieldType.ARRAY).description("봉사 참여 이력 리스트")
                                ).andWithPrefix("histories.[].",
                                        fieldWithPath("no").type(JsonFieldType.NUMBER).description("봉사 참여 이력 고유키 PK"),
                                        fieldWithPath("picture.isStaticImage").type(JsonFieldType.BOOLEAN).description("봉사모집글의 정적/동적 이미지 구분"),
                                        fieldWithPath("picture.uploadImage").type(JsonFieldType.STRING).optional().description("봉사 모집글의 업로드 이미지 URL, isStaticImage True 일 경우 NULL"),
                                        fieldWithPath("date").type(JsonFieldType.STRING).description("봉사 참여이력의 종료일자"),
                                        fieldWithPath("title").type(JsonFieldType.STRING).description("봉사 모집글의 제목"),
                                        fieldWithPath("sido").type(JsonFieldType.STRING).description("봉사 참여 이력의 시/구 코드"),
                                        fieldWithPath("sigungu").type(JsonFieldType.STRING).description("봉사 참여 이력의 시/군/구 코드"),
                                        fieldWithPath("volunteeringCategory").type(JsonFieldType.STRING).description("봉사 모집글의 봉사카테고리 코드 Code VolunteeringCategory 참고바람"),
                                        fieldWithPath("volunteeringType").type(JsonFieldType.STRING).description("봉사 모집글의 봉사 유형 코드 Code VolunteeringType 참고바람"),
                                        fieldWithPath("isIssued").type(JsonFieldType.BOOLEAN).description("봉사 모집글의 봉사 시간 인증 가능 여부"),
                                        fieldWithPath("volunteerType").type(JsonFieldType.STRING).description("봉사 모집글의 봉사자 유형 코드 Code VolunteerType 참고바람."),
                                        fieldWithPath("progressTime").type(JsonFieldType.NUMBER).description("봉사 진행 시간")
                                )
                        )
                );

    }

    @Test
    @Transactional
    @DisplayName("user2의 마이페이지 참여중인 일정 리스트 조회")
    @WithUserDetails(value = "kakao_222222", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void mypageSchedule() throws Exception {
        // when & then
        ResultActions result = mockMvc.perform(RestDocumentationRequestBuilders.get("/user/schedule")
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
                                        fieldWithPath("scheduleList").type(JsonFieldType.ARRAY).description("봉사 참여 중인 일정 리스트")
                                ).andWithPrefix("scheduleList.[].",
                                        fieldWithPath("no").type(JsonFieldType.NUMBER).description("봉사 참여 고유키 PK"),
                                        fieldWithPath("startDay").type(JsonFieldType.STRING).description("봉사 참여 일정 시작일"),
                                        fieldWithPath("sido").type(JsonFieldType.STRING).description("봉사 참여 일정의 시/구 코드"),
                                        fieldWithPath("sigungu").type(JsonFieldType.STRING).description("봉사 참여 일정의 시/군/구 코드"),
                                        fieldWithPath("details").type(JsonFieldType.STRING).description("봉사 참여 일정의 기관 상세주소"),
                                        fieldWithPath("organizationName").type(JsonFieldType.STRING).description("봉사 참여 일정의 기관 이름"),
                                        fieldWithPath("startTime").type(JsonFieldType.STRING).description("봉사 참여 일정의 봉사 시작 시간"),
                                        fieldWithPath("hourFormat").type(JsonFieldType.STRING).description("봉사 참여 일정의 시간 포멧 Code HourFormat 참고."),
                                        fieldWithPath("progressTime").type(JsonFieldType.NUMBER).description("봉사 참여 일정의 봉사 진행 시간")
                                )
                        )
                );
    }


    @Test
    @Transactional
    @DisplayName("user1의 마이페이지 참여중인 모집글 리스트 조회")
    @WithUserDetails(value = "kakao_111111", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void mypageRecruitment() throws Exception {
        // when & then
        ResultActions result = mockMvc.perform(RestDocumentationRequestBuilders.get("/user/recruitment")
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
                                        fieldWithPath("recruitmentList").type(JsonFieldType.ARRAY).description("봉사 모집글에 참여중인 이력 리스트")
                                ).andWithPrefix("recruitmentList.[].",
                                        fieldWithPath("no").type(JsonFieldType.NUMBER).description("봉사 모집글 고유키 PK"),
                                        fieldWithPath("picture.isStaticImage").type(JsonFieldType.BOOLEAN).description("봉사모집글의 정적/동적 이미지 구분"),
                                        fieldWithPath("picture.uploadImage").type(JsonFieldType.STRING).optional().description("봉사 모집글의 업로드 이미지 URL, isStaticImage True 일 경우 NULL"),
                                        fieldWithPath("startDay").type(JsonFieldType.STRING).description("봉사 모집글의 시작일"),
                                        fieldWithPath("endDay").type(JsonFieldType.STRING).description("봉사 모집글의 종료일"),
                                        fieldWithPath("title").type(JsonFieldType.STRING).description("봉사 모집글의 제목"),
                                        fieldWithPath("sido").type(JsonFieldType.STRING).description("봉사 모집글의 시/구 코드"),
                                        fieldWithPath("sigungu").type(JsonFieldType.STRING).description("봉사 모집글의 시/군/구 코드"),
                                        fieldWithPath("details").type(JsonFieldType.STRING).description("봉사 모집글의 기관 상세주소"),
                                        fieldWithPath("volunteeringCategory").type(JsonFieldType.STRING).description("봉사 모집글의 봉사 카테고리 코드 Code VolunteeringCategory 참고바람"),
                                        fieldWithPath("volunteeringType").type(JsonFieldType.STRING).description("봉사 모집글의 봉사유형 코드 Code VolunteeringType 참고바람"),
                                        fieldWithPath("isIssued").type(JsonFieldType.BOOLEAN).description("봉사 모집글의 봉사 시간 인증 가능 여부"),
                                        fieldWithPath("volunteerType").type(JsonFieldType.STRING).description("봉사 모집글의 봉사자 유형코드 Code VolunteerType 참고바람.")
                                )
                        )
                );
    }

    @Test
    @Transactional
    @DisplayName("user2의 마이페이지 임시저장 모집글 리스트 조회")
    @WithUserDetails(value = "kakao_222222", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void mypageRecruitmentTemp() throws Exception {
        // when & then
        ResultActions result = mockMvc.perform(RestDocumentationRequestBuilders.get("/user/recruitment/temp")
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
                                        fieldWithPath("recruitmentTempList").type(JsonFieldType.ARRAY).description("임시저장 봉사 모집글 리스트")
                                ).andWithPrefix("recruitmentTempList.[].",
                                        fieldWithPath("no").type(JsonFieldType.NUMBER).description("봉사 모집글 고유키 PK"),
                                        fieldWithPath("title").type(JsonFieldType.STRING).description("봉사 모집글의 제목"),
                                        fieldWithPath("writeTime").type(JsonFieldType.STRING).description("봉사 모집글 작성시간"),
                                        fieldWithPath("writeDay").type(JsonFieldType.STRING).description("봉사 모집글 작성일")
                                )
                        )
                );
    }

    @Test
    @Transactional
    @DisplayName("user1의 마이페이지 임시저장 봉사로그 리스트 조회")
    @WithUserDetails(value = "kakao_111111", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void mypageLogboardTemp() throws Exception {
        // when & then
        ResultActions result = mockMvc.perform(RestDocumentationRequestBuilders.get("/user/logboard/temp")
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
                                        fieldWithPath("logboardTempList").type(JsonFieldType.ARRAY).description("임시저장 봉사 로그 리스트")
                                ).andWithPrefix("logboardTempList.[].",
                                        fieldWithPath("no").type(JsonFieldType.NUMBER).description("봉사 로그 고유키 PK"),
                                        fieldWithPath("content").type(JsonFieldType.STRING).description("봉사 로그 내용"),
                                        fieldWithPath("writeTime").type(JsonFieldType.STRING).description("봉사 로그 임시저장 시간"),
                                        fieldWithPath("writeDay").type(JsonFieldType.STRING).description("봉사 로그 임시저장 일")
                                )
                        )
                );
    }

    @Test
    @Transactional
    @DisplayName("user2의 마이페이지 임시저장 모집글 리스트 삭제")
    @WithUserDetails(value = "kakao_222222", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void mypageRecruitmentTempDelete() throws Exception {
        // when & then
        RecruitmentListRequestParam dto = new RecruitmentListRequestParam(deleteRecruitmentNoList);
        ResultActions result = mockMvc.perform(RestDocumentationRequestBuilders.delete("/user/recruitment/temp")
                .header(AUTHORIZATION_HEADER, "access Token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(dto))
        );

        //then
        result.andExpect(status().isOk())
                .andDo(print())
                .andDo(
                        restDocs.document(
                                requestHeaders(
                                        headerWithName(AUTHORIZATION_HEADER).description("JWT Access Token")
                                ),
                                requestFields(
                                        fieldWithPath("recruitmentList").type(JsonFieldType.ARRAY).description("유저 고유키 PK")
                                )
                        )
                );
    }



    @Test
    @Transactional
    @DisplayName("user1의 마이페이지 임시저장 봉사로그 리스트 삭제")
    @WithUserDetails(value = "kakao_111111", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void mypageLogboardTempDelete() throws Exception {
        // when & then
        LogboardListRequestParam dto = new LogboardListRequestParam(deleteLogboardNoList);
        ResultActions result = mockMvc.perform(RestDocumentationRequestBuilders.delete("/user/logboard/temp")
                .header(AUTHORIZATION_HEADER, "access Token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(dto))
        );

        //then
        result.andExpect(status().isOk())
                .andDo(print())
                .andDo(
                        restDocs.document(
                                requestHeaders(
                                        headerWithName(AUTHORIZATION_HEADER).description("JWT Access Token")
                                ),
                                requestFields(
                                        fieldWithPath("logboardList[]").type(JsonFieldType.ARRAY).description("유저 고유키 PK")
                                )
                        )
                );
    }

    @Disabled
    @Test
    @DisplayName("user2의 마이페이지 임시저장 모집글 번호 누락으로 리스트 삭제 실패")
    @WithUserDetails(value = "kakao_222222", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void myPage_temp_recruitment_delete_fail() throws Exception {
        // when & then
        mockMvc.perform(
                        delete("/user/recruitment/temp"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Disabled
    @Test
    @DisplayName("user1의 마이페이지 임시저장 봉사로그 번호 누락으로 리스트 삭제 실패")
    @WithUserDetails(value = "kakao_111111", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void myPage_temp_logboard_delete_fail() throws Exception {
        // when & then
        mockMvc.perform(
                        delete("/user/logboard/temp"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    public RecruitmentParam makeRecruitmentParam(int num, int progressTime, boolean published){
        return new RecruitmentParam(
                 "recruitment title "+num
                , "recruitment content "+num
                , VolunteeringCategory.ADMINSTRATION_ASSISTANCE
                , VolunteeringType.REG
                , VolunteerType.ALL
                , 10
                , true
                , "recruitment organizationName "+num
                , Address.createAddress("Rido"+num,"Rigungu"+num,"detail"+num, "fullName"+num)
                , Coordinate.createCoordinate(Float.valueOf(num+num+num+num), Float.valueOf(num+num+num))
                , Timetable.createTimetable(LocalDate.now(), LocalDate.now().plusMonths(1), HourFormat.AM, LocalTime.now(), progressTime)
                , published);
    }

    public ScheduleUpsertCommand makeScheduleParam(int num, int progressTime){
        return new ScheduleUpsertCommand(
                  Timetable.createTimetable(LocalDate.now(), LocalDate.now(), HourFormat.AM, LocalTime.now(), progressTime)
                , "schedule organizationName"+num
                , Address.createAddress("Sido"+num, "Sigungu"+num, "details"+num, "fullName"+num)
                , "schedule content"+num
                , 10);
    }

    private <T> String toJson(T data) throws JsonProcessingException {
        return objectMapper.writeValueAsString(data);
    }

    @AfterEach
    public void clearList(){
        deleteLogboardNoList = new ArrayList<>();
        deleteRecruitmentNoList = new ArrayList<>();
    }
}
