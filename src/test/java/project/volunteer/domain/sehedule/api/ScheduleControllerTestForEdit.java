package project.volunteer.domain.sehedule.api;

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
import project.volunteer.domain.scheduleParticipation.dao.ScheduleParticipationRepository;
import project.volunteer.domain.scheduleParticipation.domain.ScheduleParticipation;
import project.volunteer.domain.sehedule.api.dto.request.AddressRequest;
import project.volunteer.domain.sehedule.api.dto.request.ScheduleRequest;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ScheduleControllerTestForEdit {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @PersistenceContext EntityManager em;
    @Autowired UserRepository userRepository;
    @Autowired RecruitmentRepository recruitmentRepository;
    @Autowired ScheduleRepository scheduleRepository;
    @Autowired ParticipantRepository participantRepository;
    @Autowired ScheduleParticipationRepository scheduleParticipationRepository;

    Schedule saveSchedule;
    private void clear() {
        em.flush();
        em.clear();
    }
    @BeforeEach
    private void setUp() {
        //작성자 저장
        User writer = User.createUser("1234", "1234", "1234", "1234", Gender.M, LocalDate.now(), "1234",
                true, true, true, Role.USER, "kakao", "1234", null);
        userRepository.save(writer);

        //Embedded 값 세팅
        Address address = Address.createAddress("1", "111", "test");
        Timetable timetable = Timetable.createTimetable(LocalDate.now(), LocalDate.now(), HourFormat.AM, LocalTime.now(), 10);
        Coordinate coordinate = Coordinate.createCoordinate(3.2F, 3.2F);

        //봉사 모집글 저장
        Recruitment saveRecruitment =
                Recruitment.createRecruitment("test", "test", VolunteeringCategory.EDUCATION, VolunteeringType.IRREG,
                        VolunteerType.TEENAGER, 10, true, "test", address, coordinate, timetable, true);
        saveRecruitment.setWriter(writer);
        recruitmentRepository.save(saveRecruitment);

        //일정 등록
        saveSchedule = Schedule.createSchedule(timetable, "test", "organizaion", address, 8);
        saveSchedule.setRecruitment(saveRecruitment);
        scheduleRepository.save(saveSchedule);

        //봉사 팀원 및 일정 참여자 등록
        for(int i=0; i<5;i++){
            User user = User.createUser("test" + i, "test" + i, "test" + i, "test" + i, Gender.M, LocalDate.now(),
                    "test" + i, true, true, true, Role.USER, "kakao", "test" + i, null);
            userRepository.save(user);

            Participant participant = Participant.createParticipant(saveRecruitment, user, State.JOIN_APPROVAL);
            participantRepository.save(participant);

            ScheduleParticipation scheduleParticipation =
                    ScheduleParticipation.createScheduleParticipation(saveSchedule, participant, State.PARTICIPATION_APPROVAL);
            scheduleParticipationRepository.save(scheduleParticipation);
        }

        clear();
    }

    @DisplayName("봉사 일정 수정에 성공하다.")
    @Test
    @Transactional
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void scheduleEdit() throws Exception {
        //given
        final Long scheduleNo = saveSchedule.getScheduleNo();
        final String sido = "1";
        final String sigungu = "1111";
        final String details = "details";
        final String startDay = "05-26-2023";
        final String hourFormat = "AM";
        final String startTime = "10:00";
        final Integer progressTime = 2;
        final String organizationName = "organization";
        final Integer volunteerNum = 6;
        final String content = "content";
        ScheduleRequest dto = new ScheduleRequest(scheduleNo, new AddressRequest(sido, sigungu, details), startDay, hourFormat, startTime, progressTime,
                organizationName, volunteerNum, content);

        //when & then
        mockMvc.perform(put("/schedule")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(toJson(dto)))
                .andExpect(status().isOk());
    }

    @DisplayName("방장이 아닌 사용자가 일정 수정을 시도하다.")
    @Test
    @Transactional
    @WithMockCustomUser(tempValue = "forbidden")
    public void forbidden() throws Exception {
        //given
        final Long scheduleNo = saveSchedule.getScheduleNo();
        final String sido = "1";
        final String sigungu = "1111";
        final String details = "details";
        final String startDay = "05-26-2023";
        final String hourFormat = "AM";
        final String startTime = "10:00";
        final Integer progressTime = 2;
        final String organizationName = "organization";
        final Integer volunteerNum = 6;
        final String content = "content";
        ScheduleRequest dto = new ScheduleRequest(scheduleNo, new AddressRequest(sido, sigungu, details), startDay, hourFormat, startTime, progressTime,
                organizationName, volunteerNum, content);

        //when & then
        mockMvc.perform(put("/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(dto)))
                .andExpect(status().isForbidden())
                .andDo(print());
    }

    @DisplayName("봉사 일정 수정간 모집 인원은 현재 일정 참여자 수보다 적을 수 없다.")
    @Test
    @Transactional
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void exceedVolunteerNum() throws Exception {
        //given
        final Long scheduleNo = saveSchedule.getScheduleNo();
        final String sido = "1";
        final String sigungu = "1111";
        final String details = "details";
        final String startDay = "05-26-2023";
        final String hourFormat = "AM";
        final String startTime = "10:00";
        final Integer progressTime = 2;
        final String organizationName = "organization";
        final Integer volunteerNum = 3; //현재 일정에 참여중인 인원수(5명) 보다 작을 수 없다.!
        final String content = "content";
        ScheduleRequest dto = new ScheduleRequest(scheduleNo, new AddressRequest(sido, sigungu, details), startDay, hourFormat, startTime, progressTime,
                organizationName, volunteerNum, content);

        //when & then
        mockMvc.perform(put("/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(dto)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    private <T> String toJson(T data) throws JsonProcessingException {
        return objectMapper.writeValueAsString(data);
    }

    @DisplayName("잘못된 MediaType 으로 일정 수정 API를 요청하다.")
    @Test
    @Transactional
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void invalidMediaType() throws Exception {
        //given
        final Long scheduleNo = saveSchedule.getScheduleNo();
        final String sido = "1";
        final String sigungu = "1111";
        final String details = "details";
        final String startDay = "05-26-2023";
        final String hourFormat = "AM";
        final String startTime = "10:00";
        final Integer progressTime = 2;
        final String organizationName = "organization";
        final Integer volunteerNum = 6;
        final String content = "content";
        ScheduleRequest dto = new ScheduleRequest(scheduleNo, new AddressRequest(sido, sigungu, details), startDay, hourFormat, startTime, progressTime,
                organizationName, volunteerNum, content);

        //when & then
        mockMvc.perform(put("/schedule")
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE) //잘못된 Media Type!
                        .content(toJson(dto)))
                .andExpect(status().isUnsupportedMediaType())
                .andDo(print());
    }
}