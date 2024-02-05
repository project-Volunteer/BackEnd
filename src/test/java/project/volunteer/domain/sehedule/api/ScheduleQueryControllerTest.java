package project.volunteer.domain.sehedule.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
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
import project.volunteer.domain.scheduleParticipation.repository.ScheduleParticipationRepository;
import project.volunteer.domain.scheduleParticipation.domain.ScheduleParticipation;
import project.volunteer.domain.sehedule.repository.ScheduleRepository;
import project.volunteer.domain.sehedule.domain.Schedule;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.domain.Gender;
import project.volunteer.domain.user.domain.Role;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.*;
import project.volunteer.global.common.dto.StateResponse;
import project.volunteer.global.test.WithMockCustomUser;
import project.volunteer.document.restdocs.config.RestDocsConfiguration;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import(RestDocsConfiguration.class)
class ScheduleQueryControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;
    @Autowired RecruitmentRepository recruitmentRepository;
    @Autowired ParticipantRepository participantRepository;
    @Autowired ScheduleRepository scheduleRepository;
    @Autowired ScheduleParticipationRepository scheduleParticipationRepository;

    Recruitment saveRecruitment;
    List<Participant> teamMember = new ArrayList<>();

    @BeforeEach
    public void setup(){
        //작성자 저장
        User writer = User.createUser("sctfq1234", "sctfq1234", "sctfq1234", "sctfq1234", Gender.M, LocalDate.now(), "1234",
                true, true, true, Role.USER, "kakao", "sctfq1234", null);
        userRepository.save(writer);

        //Embedded 값 세팅
        Address recruitmentAddress = Address.createAddress("1", "111", "test", "fullName");
        Timetable recruitmentTimetable = Timetable.createTimetable(LocalDate.now(), LocalDate.now(), HourFormat.AM, LocalTime.now(), 10);
        Coordinate coordinate = Coordinate.createCoordinate(3.2F, 3.2F);

        //봉사 모집글 저장
        saveRecruitment =
                Recruitment.createRecruitment("test", "test", VolunteeringCategory.EDUCATION, VolunteeringType.IRREG,
                        VolunteerType.TEENAGER, 10, true, "test", recruitmentAddress, coordinate, recruitmentTimetable, true);
        saveRecruitment.setWriter(writer);
        recruitmentRepository.save(saveRecruitment);

        //봉사 팀원 저장
        for(int i=0;i<5;i++){
            User createUser = User.createUser("sctfq" + i, "sctfq" + i, "sctfq" + i, "sctfq" + i, Gender.M, LocalDate.now(), "picture" + i,
                    true, true, true, Role.USER, "kakao", "sctfq" + i, null);
            User saveUser = userRepository.save(createUser);

            Participant createParticipant = Participant.createParticipant(saveRecruitment, saveUser, ParticipantState.JOIN_APPROVAL);
            teamMember.add(participantRepository.save(createParticipant));
        }
    }

    private Schedule 스케줄_등록(LocalDate startDay, int volunteerNum){
        Timetable timetable = Timetable.createTimetable(startDay, startDay, HourFormat.AM, LocalTime.now(), 10);
        Address address = Address.createAddress("1", "111", "test", "fullName");

        Schedule schedule = Schedule.create(saveRecruitment, timetable, "test" ,"test", address, volunteerNum);
        return scheduleRepository.save(schedule);
    }
    private ScheduleParticipation 스케줄_참여자_등록(Schedule schedule, Participant participant, ParticipantState state){
        ScheduleParticipation sp = ScheduleParticipation.createScheduleParticipation(schedule, participant, state);
        return scheduleParticipationRepository.save(sp);
    }

    @Disabled
    @Test
    @Transactional
    @DisplayName("방장이 모집중인 가장 가까운 일정 상세 조회에 성공한다.")
    @WithUserDetails(value = "sctfq1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void scheduleDetailsByOwner() throws Exception {
        //given
        스케줄_등록(LocalDate.now().plusMonths(2), 2);

        //when & then
        mockMvc.perform(get("/recruitment/{recruitmentNo}/schedule/",saveRecruitment.getRecruitmentNo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("activeVolunteerNum").value(0))
                .andExpect(jsonPath("state").value(StateResponse.AVAILABLE.name()))
                .andDo(print());
    }

    @Disabled
    @Test
    @Transactional
    @DisplayName("팀원 아닌 사용자가 일정 조회를 시도한다.")
    @WithMockCustomUser(tempValue = "sctfq_forbidden")
    public void forbiddenScheduleDetails() throws Exception {
        //given
        스케줄_등록(LocalDate.now().plusMonths(2), 2);

        //when & then
        mockMvc.perform(get("/recruitment/{recruitmentNo}/schedule/",saveRecruitment.getRecruitmentNo()))
                .andExpect(status().isForbidden())
                .andDo(print());
    }

    @Disabled
    @Test
    @Transactional
    @DisplayName("모집중인 가장 가까운 일정이 존재하지 않는다.")
    @WithUserDetails(value = "sctfq0", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void nullScheduleDetails() throws Exception {
        //given
        스케줄_등록(LocalDate.now(), 2);

        //when & then
        mockMvc.perform(get("/recruitment/{recruitmentNo}/schedule/",saveRecruitment.getRecruitmentNo()))
                .andExpect(status().isOk())
                .andExpect(content().string(""))
                .andDo(print());
    }

    @Disabled
    @Test
    @Transactional
    @DisplayName("인원 모집 마감된 일정을 조회하다.")
    @WithUserDetails(value = "sctfq0", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 일정상세조회시_참여자상태_마감() throws Exception {
        //given
        Schedule schedule = 스케줄_등록(LocalDate.now().plusMonths(2), 1);
        스케줄_참여자_등록(schedule, teamMember.get(1), ParticipantState.PARTICIPATING);
        schedule.increaseParticipant();

        //when & then
        mockMvc.perform(get("/recruitment/{recruitmentNo}/schedule/",saveRecruitment.getRecruitmentNo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("state").value(StateResponse.FULL.name()))
                .andDo(print());
    }

    @Disabled
    @Test
    @Transactional
    @DisplayName("참여가능한 일정을 조회하다.")
    @WithUserDetails(value = "sctfq0", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 일정상세조회시_참여자상태_참여가능() throws Exception {
        //given
        Schedule schedule = 스케줄_등록(LocalDate.now().plusMonths(2), 1);

        //when & then
        mockMvc.perform(get("/recruitment/{recruitmentNo}/schedule/",saveRecruitment.getRecruitmentNo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("state").value(StateResponse.AVAILABLE.name()))
                .andDo(print());
    }

    @Disabled
    @Test
    @Transactional
    @DisplayName("참여 취소 요청한 일정을 조회하다.")
    @WithUserDetails(value = "sctfq0", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 일정상세조회시_참여자상태_취소요청() throws Exception {
        //given
        Schedule schedule = 스케줄_등록(LocalDate.now().plusMonths(2), 1);
        스케줄_참여자_등록(schedule, teamMember.get(0), ParticipantState.PARTICIPATION_CANCEL);
        schedule.increaseParticipant();

        //when & then
        mockMvc.perform(get("/recruitment/{recruitmentNo}/schedule/",saveRecruitment.getRecruitmentNo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("state").value(StateResponse.CANCELLING.name()))
                .andDo(print());
    }

    @Disabled
    @Test
    @Transactional
    @DisplayName("참여중인 일정을 조회하다.")
    @WithUserDetails(value = "sctfq0", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 일정상세조회시_참여자상태_참여중() throws Exception {
        //given
        Schedule schedule = 스케줄_등록(LocalDate.now().plusMonths(2), 2);
        스케줄_참여자_등록(schedule, teamMember.get(0), ParticipantState.PARTICIPATING);
        schedule.increaseParticipant();

        //when & then
        mockMvc.perform(get("/recruitment/{recruitmentNo}/schedule/",saveRecruitment.getRecruitmentNo()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("state").value(StateResponse.PARTICIPATING.name()))
                .andDo(print());
    }

    @Disabled
    @Test
    @Transactional
    @DisplayName("캘린더 일정 리스트 조회 간 필수 파라미터를 누락하다.")
    @WithUserDetails(value = "sctfq0", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void missingQueryParam() throws Exception {
        mockMvc.perform(get("/recruitment/{recruitmentNo}/calendar",saveRecruitment.getRecruitmentNo())
                        .queryParam("year", "2023")) //"mon" 쿼리 스트링 누락
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

}