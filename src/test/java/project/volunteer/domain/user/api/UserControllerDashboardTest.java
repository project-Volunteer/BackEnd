package project.volunteer.domain.user.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.logboard.application.LogboardService;
import project.volunteer.domain.participation.dao.ParticipantRepository;
import project.volunteer.domain.participation.domain.Participant;
import project.volunteer.domain.recruitment.application.RecruitmentService;
import project.volunteer.domain.recruitment.application.dto.RecruitmentParam;
import project.volunteer.domain.recruitment.dao.RecruitmentRepository;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.scheduleParticipation.dao.ScheduleParticipationRepository;
import project.volunteer.domain.scheduleParticipation.domain.ScheduleParticipation;
import project.volunteer.domain.sehedule.application.ScheduleService;
import project.volunteer.domain.sehedule.application.dto.ScheduleParam;
import project.volunteer.domain.sehedule.dao.ScheduleRepository;
import project.volunteer.domain.user.api.dto.request.LogboardListRequestParam;
import project.volunteer.domain.user.api.dto.request.RecruitmentListRequestParam;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.domain.Gender;
import project.volunteer.domain.user.domain.Role;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.*;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class UserControllerDashboardTest {
    @PersistenceContext EntityManager em;
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired ParticipantRepository participantRepository;
    @Autowired RecruitmentService recruitmentService;
    @Autowired RecruitmentRepository recruitmentRepository;
    @Autowired ScheduleService scheduleService;
    @Autowired ScheduleRepository scheduleRepository;
    @Autowired ScheduleParticipationRepository spRepository;
    @Autowired LogboardService logboardService;

    private static User user1;
    private static User user2;
    private static List<Long> deleteRecruitmentNoList = new ArrayList<>();
    private static List<Long> deleteLogboardNoList = new ArrayList<>();


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
        Long rNo1 = recruitmentService.addRecruitment(user1, makeRecruitmentParam(1, 3 , true )).getRecruitmentNo();
        Long rNo2 = recruitmentService.addRecruitment(user1, makeRecruitmentParam(2, 3 , true )).getRecruitmentNo();
        Long rNo3 = recruitmentService.addRecruitment(user1, makeRecruitmentParam(3, 3 , false )).getRecruitmentNo();
        Long rNo4 = recruitmentService.addRecruitment(user2, makeRecruitmentParam(4, 2 , true )).getRecruitmentNo();
        Long rNo5 = recruitmentService.addRecruitment(user2, makeRecruitmentParam(5, 2 , true )).getRecruitmentNo();
        Long rNo6 = recruitmentService.addRecruitment(user2, makeRecruitmentParam(6, 2 , true )).getRecruitmentNo();
        Long rNo7 = recruitmentService.addRecruitment(user2, makeRecruitmentParam(7, 1 , true )).getRecruitmentNo();

        Long rNo8 = recruitmentService.addRecruitment(user2, makeRecruitmentParam(8, 3 , false )).getRecruitmentNo();
        Long rNo9 = recruitmentService.addRecruitment(user2, makeRecruitmentParam(9, 3 , false )).getRecruitmentNo();
        Long rNo10 = recruitmentService.addRecruitment(user2, makeRecruitmentParam(0, 3 , false )).getRecruitmentNo();
        Long rNo11 = recruitmentService.addRecruitment(user2, makeRecruitmentParam(1, 3 , false )).getRecruitmentNo();

        deleteRecruitmentNoList.add(rNo9);
        deleteRecruitmentNoList.add(rNo10);
        deleteRecruitmentNoList.add(rNo11);



        /*  봉사 모집 참여
			모집글 1, 2 - 작성자 본인(사용자1) 참여 완료
			모집글 4, 5 - 사용자1이 참여 요청
			모집글 6, 7 - 사용자1이 승인 완료
			모집글 1 - 사용자2가 승인 완료
			모집글 2 - 사용자2가 참여 요청 */
        Participant r1p1 = participantRepository.save(Participant.builder()
                .participant(user1).state(ParticipantState.JOIN_APPROVAL).recruitment(recruitmentRepository.findById(rNo1).get())
                .build());
        Participant r2p2 = participantRepository.save(Participant.builder()
                .participant(user1).state(ParticipantState.JOIN_APPROVAL).recruitment(recruitmentRepository.findById(rNo2).get())
                .build());
        Participant r4p3 = participantRepository.save(Participant.builder()
                .participant(user1).state(ParticipantState.JOIN_REQUEST).recruitment(recruitmentRepository.findById(rNo4).get())
                .build());
        Participant r5p4 =  participantRepository.save(Participant.builder()
                .participant(user1).state(ParticipantState.JOIN_REQUEST).recruitment(recruitmentRepository.findById(rNo5).get())
                .build());
        Participant r6p5 = participantRepository.save(Participant.builder()
                .participant(user1).state(ParticipantState.JOIN_APPROVAL).recruitment(recruitmentRepository.findById(rNo6).get())
                .build());
        Participant r7p6 = participantRepository.save(Participant.builder()
                .participant(user1).state(ParticipantState.JOIN_APPROVAL).recruitment(recruitmentRepository.findById(rNo7).get())
                .build());
        Participant r1p7 = participantRepository.save(Participant.builder()
                .participant(user2).state(ParticipantState.JOIN_APPROVAL).recruitment(recruitmentRepository.findById(rNo1).get())
                .build());
        Participant r2p8 = participantRepository.save(Participant.builder()
                .participant(user2).state(ParticipantState.JOIN_REQUEST).recruitment(recruitmentRepository.findById(rNo2).get())
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
    @DisplayName("user1의 마이페이지 대시보드 조회")
    @WithUserDetails(value = "kakao_111111", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void myPage_dashboard() throws Exception {
        // when & then
        mockMvc.perform(
                get("/user/info"))
                .andExpect(status().isOk())
                .andDo(print());

        /*
            {
                "userInfo":{"nicName":"nickname11","email":"email11@gmail.com","profile":"picture1111"}
                ,"historyTimeInfo":{"totalTime":15,"totalCnt":6}
                ,"activityInfo":{"joinApprovalCnt":4,"joinRequestCnt":2,"recruitingCnt":2,"tempSavingCnt":4}
            }
        */

    }

    @Test
    @DisplayName("user2의 마이페이지 대시보드 조회")
    @WithUserDetails(value = "kakao_222222", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void myPage_dashboard2() throws Exception {
        // when & then
        mockMvc.perform(
                        get("/user/info"))
                .andExpect(status().isOk())
                .andDo(print());

        /*
            {
                "userInfo":{"nicName":"nickname22","email":"email22@gmail.com","profile":"picture2222"}
                ,"historyTimeInfo":{"totalTime":3,"totalCnt":1}
                ,"activityInfo":{"joinApprovalCnt":1,"joinRequestCnt":1,"recruitingCnt":4,"tempSavingCnt":1}
            }
        */

    }

    @Test
    @DisplayName("user1의 마이페이지 봉사이력 조회")
    @WithUserDetails(value = "kakao_111111", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void myPage_hisotry() throws Exception {
        // when & then
        mockMvc.perform(
                get("/user/history?page=1"))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("user2의 마이페이지 참여중인 일정 리스트 조회")
    @WithUserDetails(value = "kakao_222222", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void myPage_join_schedule() throws Exception {
        // when & then
        mockMvc.perform(
                        get("/user/schedule"))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("user1의 마이페이지 참여중인 모집글 리스트 조회")
    @WithUserDetails(value = "kakao_111111", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void myPage_join_recruitment() throws Exception {
        // when & then
        mockMvc.perform(
                        get("/user/recruitment"))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("user2의 마이페이지 임시저장 모집글 리스트 조회")
    @WithUserDetails(value = "kakao_222222", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void myPage_temp_recruitment() throws Exception {
        // when & then
        mockMvc.perform(
                        get("/user/recruitment/temp"))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("user1의 마이페이지 임시저장 봉사로그 리스트 조회")
    @WithUserDetails(value = "kakao_111111", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void myPage_temp_logboard() throws Exception {
        // when & then
        mockMvc.perform(
                        get("/user/logboard/temp"))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Disabled
    @Test
    @DisplayName("user2의 마이페이지 임시저장 모집글 리스트 삭제")
    @WithUserDetails(value = "kakao_222222", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void myPage_temp_recruitment_delete() throws Exception {
        RecruitmentListRequestParam dto = new RecruitmentListRequestParam(deleteRecruitmentNoList);
        // when
        mockMvc.perform(
                        delete("/user/recruitment/temp")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(dto)))
                .andExpect(status().isOk())
                .andDo(print());
        // then
        //TODO: http body에 필수로 dto가 필요한데, 누락된건가요? build test 시 실패하네요!
//        mockMvc.perform(
//                        get("/user/recruitment/temp"))
//                .andExpect(status().isOk())
//                .andDo(print());

    }

    @Test
    @DisplayName("user1의 마이페이지 임시저장 봉사로그 리스트 삭제")
    @WithUserDetails(value = "kakao_111111", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void myPage_temp_logboard_delete() throws Exception {
        LogboardListRequestParam dto = new LogboardListRequestParam(deleteLogboardNoList);
        // when
        mockMvc.perform(
                        delete("/user/logboard/temp")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(dto)))
                .andExpect(status().isOk())
                .andDo(print());

        // then
        mockMvc.perform(
                        get("/user/logboard/temp"))
                .andExpect(status().isOk())
                .andDo(print());

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
                , Address.createAddress("Rido"+num,"Rigungu"+num,"detail"+num)
                , Coordinate.createCoordinate(Float.valueOf(num+num+num+num), Float.valueOf(num+num+num))
                , Timetable.createTimetable(LocalDate.now(), LocalDate.now().plusMonths(1), HourFormat.AM, LocalTime.now(), progressTime)
                , published);
    }

    public ScheduleParam makeScheduleParam(int num, int progressTime){
        return new ScheduleParam(
                  Timetable.createTimetable(LocalDate.now(), LocalDate.now(), HourFormat.AM, LocalTime.now(), progressTime)
                , "schedule organizationName"+num
                , Address.createAddress("Sido"+num, "Sigungu"+num, "details"+num)
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
