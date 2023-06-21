package project.volunteer.domain.scheduleParticipation.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.participation.dao.ParticipantRepository;
import project.volunteer.domain.participation.domain.Participant;
import project.volunteer.domain.recruitment.dao.RecruitmentRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.scheduleParticipation.api.dto.CancelApproval;
import project.volunteer.domain.scheduleParticipation.api.dto.CompleteApproval;
import project.volunteer.domain.scheduleParticipation.dao.ScheduleParticipationRepository;
import project.volunteer.domain.scheduleParticipation.domain.ScheduleParticipation;
import project.volunteer.domain.scheduleParticipation.service.ScheduleParticipationService;
import project.volunteer.domain.sehedule.dao.ScheduleRepository;
import project.volunteer.domain.sehedule.domain.Schedule;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.domain.Gender;
import project.volunteer.domain.user.domain.Role;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.*;
import project.volunteer.global.test.WithMockCustomUser;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ScheduleParticipantControllerTest {

    @Autowired MockMvc mockMvc;
    @PersistenceContext EntityManager em;
    @Autowired UserRepository userRepository;
    @Autowired RecruitmentRepository recruitmentRepository;
    @Autowired ScheduleRepository scheduleRepository;
    @Autowired ParticipantRepository participantRepository;
    @Autowired ScheduleParticipationRepository scheduleParticipationRepository;
    @Autowired ScheduleParticipationService spService;
    @Autowired ObjectMapper objectMapper;

    private User writer;
    private User loginUser;
    private Recruitment saveRecruitment;
    private Schedule saveSchedule;
    @BeforeEach
    void init(){
        //작성자 저장
        User writerUser = User.createUser("1234", "1234", "1234", "1234", Gender.M, LocalDate.now(), "1234",
                true, true, true, Role.USER, "kakao", "1234", null);
        writer = userRepository.save(writerUser);

        //모집글 저장
        Recruitment createRecruitment = Recruitment.createRecruitment("title", "content", VolunteeringCategory.CULTURAL_EVENT, VolunteeringType.IRREG,
                VolunteerType.TEENAGER, 3, true, "organization",
                Address.createAddress("11", "1111","details"), Coordinate.createCoordinate(3.2F, 3.2F),
                Timetable.createTimetable(LocalDate.now(), LocalDate.now().plusMonths(3), HourFormat.AM, LocalTime.now(), 3), true);
        createRecruitment.setWriter(writer);
        saveRecruitment = recruitmentRepository.save(createRecruitment);

        //일정 저장
        Schedule createSchedule = Schedule.createSchedule(
                Timetable.createTimetable(
                        LocalDate.now().plusMonths(1), LocalDate.now().plusMonths(1),
                        HourFormat.AM, LocalTime.now(), 3),
                "content", "organization",
                Address.createAddress("11", "1111", "details"), 3);
        createSchedule.setRecruitment(saveRecruitment);
        saveSchedule = scheduleRepository.save(createSchedule);

        //로그인 유저 저장
        User login = User.createUser("test", "test", "test", "test", Gender.M, LocalDate.now(), "test",
                true, true, true, Role.USER, "kakao", "test", null);
        loginUser = userRepository.save(login);
    }

    @Test
    @DisplayName("일정 참가 신청에 성공하다.")
    @Transactional
    @WithUserDetails(value = "test", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void schedule_participate() throws Exception {
        //given
        봉사모집글_팀원_등록(saveRecruitment, loginUser);
        clear();

        //when & then
        mockMvc.perform(put("/recruitment/{recruitmentNo}/schedule/{scheduleNo}/join", saveRecruitment.getRecruitmentNo(), saveSchedule.getScheduleNo()))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("팀원이 아닌 사용자가 일정 참가 신청을 시도하다.")
    @Transactional
    @WithMockCustomUser(tempValue = "forbidden")
    public void schedule_participate_forbidden() throws Exception {

        mockMvc.perform(put("/recruitment/{recruitmentNo}/schedule/{scheduleNo}/join", saveRecruitment.getRecruitmentNo(), saveSchedule.getScheduleNo()))
                .andExpect(status().isForbidden())
                .andDo(print());
    }

    @Test
    @DisplayName("일정 참가 취소 요청에 성공하다.")
    @Transactional
    @WithUserDetails(value = "test", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void cancelParticipation() throws Exception {
        //given
        Participant participant = 봉사모집글_팀원_등록(saveRecruitment, loginUser);
        일정_참여상태_추가(saveSchedule, participant, State.PARTICIPATING);
        clear();

        //when & then
        mockMvc.perform(put("/recruitment/{recruitmentNo}/schedule/{scheduleNo}/cancel", saveRecruitment.getRecruitmentNo(), saveSchedule.getScheduleNo()))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("일정 참가 취소 요청 승인에 성공하다.")
    @Transactional
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void cancelApprove() throws Exception {
        //given
        Participant newParticipant = 봉사모집글_팀원_등록(saveRecruitment, loginUser);
        ScheduleParticipation newSp = 일정_참여상태_추가(saveSchedule, newParticipant, State.PARTICIPATION_CANCEL);
        CancelApproval dto = new CancelApproval(newSp.getScheduleParticipationNo());
        clear();

        //when & then
        mockMvc.perform(put("/recruitment/{recruitmentNo}/schedule/{scheduleNo}/cancelling", saveRecruitment.getRecruitmentNo(), saveSchedule.getScheduleNo())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(dto)))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("방장이 아닌 사용자가 일정 참가 취소 요청 승인을 시도하다.")
    @Transactional
    @WithUserDetails(value = "test", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void cancelApprove_forbidden() throws Exception {
        //given
        Participant newParticipant = 봉사모집글_팀원_등록(saveRecruitment, loginUser);
        ScheduleParticipation newSp = 일정_참여상태_추가(saveSchedule, newParticipant, State.PARTICIPATION_CANCEL);
        CancelApproval dto = new CancelApproval(newSp.getScheduleParticipationNo());
        clear();

        //when & then
        mockMvc.perform(put("/recruitment/{recruitmentNo}/schedule/{scheduleNo}/cancelling", saveRecruitment.getRecruitmentNo(), saveSchedule.getScheduleNo())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(dto)))
                .andExpect(status().isForbidden())
                .andDo(print());
    }

    @Test
    @DisplayName("일정 참가 취소 요청 승인시 필수 파라미터를 누락하다.")
    @Transactional
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void cancelApprove_notValid() throws Exception {
        //given
        Participant newParticipant = 봉사모집글_팀원_등록(saveRecruitment, loginUser);
        ScheduleParticipation newSp = 일정_참여상태_추가(saveSchedule, newParticipant, State.PARTICIPATION_CANCEL);
        CancelApproval dto = new CancelApproval(null); //필수 파라미터 누락
        clear();

        //when & then
        mockMvc.perform(put("/recruitment/{recruitmentNo}/schedule/{scheduleNo}/cancelling", saveRecruitment.getRecruitmentNo(), saveSchedule.getScheduleNo())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(dto)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("일정 참가 완료 승인에 성공하다.")
    @Transactional
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void completeApprove() throws Exception {
        //given
        Participant newParticipant = 봉사모집글_팀원_등록(saveRecruitment, loginUser);
        ScheduleParticipation newSp = 일정_참여상태_추가(saveSchedule, newParticipant, State.PARTICIPATION_COMPLETE_UNAPPROVED);
        CompleteApproval dto = new CompleteApproval(List.of(newSp.getScheduleParticipationNo()));
        clear();

        //when & then
        mockMvc.perform(put("/recruitment/{recruitmentNo}/schedule/{scheduleNo}/complete", saveRecruitment.getRecruitmentNo(), saveSchedule.getScheduleNo())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(dto)))
                .andExpect(status().isOk())
                .andDo(print());
    }


    private Participant 봉사모집글_팀원_등록(Recruitment recruitment, User user){
        Participant participant = Participant.createParticipant(recruitment, user, State.JOIN_APPROVAL);
        return participantRepository.save(participant);
    }
    private ScheduleParticipation 일정_참여상태_추가(Schedule schedule, Participant participant, State state){
        ScheduleParticipation sp = ScheduleParticipation.createScheduleParticipation(saveSchedule, participant, state);
        return scheduleParticipationRepository.save(sp);
    }
    private void clear() {
        em.flush();
        em.clear();
    }
    private <T> String toJson(T data) throws JsonProcessingException {
        return objectMapper.writeValueAsString(data);
    }
}