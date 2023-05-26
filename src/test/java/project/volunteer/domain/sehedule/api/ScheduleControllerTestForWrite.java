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
import project.volunteer.domain.recruitment.dao.RecruitmentRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.sehedule.api.dto.request.AddressSave;
import project.volunteer.domain.sehedule.api.dto.request.ScheduleSave;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.domain.Gender;
import project.volunteer.domain.user.domain.Role;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.*;
import project.volunteer.global.test.WithMockCustomUser;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ScheduleControllerTestForWrite {

    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;
    @Autowired RecruitmentRepository recruitmentRepository;
    @Autowired ObjectMapper objectMapper;

    Recruitment saveRecruitment;
    @BeforeEach
    public void setup(){
        //작성자 저장
        User writer = User.createUser("1234", "1234", "1234", "1234", Gender.M, LocalDate.now(), "1234",
                true, true, true, Role.USER, "kakao", "1234", null);
        userRepository.save(writer);

        //Embedded 값 세팅
        Address recruitmentAddress = Address.createAddress("1", "111", "test");
        Timetable recruitmentTimetable = Timetable.createTimetable(LocalDate.now(), LocalDate.now(), HourFormat.AM, LocalTime.now(), 10);
        Coordinate coordinate = Coordinate.createCoordinate(3.2F, 3.2F);

        //봉사 모집글 저장
        saveRecruitment =
                Recruitment.createRecruitment("test", "test", VolunteeringCategory.EDUCATION, VolunteeringType.IRREG,
                        VolunteerType.TEENAGER, 10, true, "test", recruitmentAddress, coordinate, recruitmentTimetable, true);
        saveRecruitment.setWriter(writer);
        recruitmentRepository.save(saveRecruitment);
    }

    @DisplayName("수동 일정 등록에 성공하다.")
    @Test
    @Transactional
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void scheduleAdd() throws Exception {
        //given
        final Long recruitmentNo = saveRecruitment.getRecruitmentNo();
        final String sido = "1";
        final String sigungu = "1111";
        final String details = "details";
        final String startDay = "05-26-2023";
        final String hourFormat = "AM";
        final String startTime = "10:00";
        final Integer progressTime = 2;
        final String organizationName = "organization";
        final Integer volunteerNum = 10;
        final String content = "content";
        ScheduleSave dto = new ScheduleSave(recruitmentNo, new AddressSave(sido, sigungu, details), startDay, hourFormat, startTime, progressTime,
                organizationName, volunteerNum, content);

        //when & then
        mockMvc.perform(post("/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(dto)))
                .andExpect(status().isOk());
    }

    @DisplayName("방장이 아닌 사용자가 수동 일정 등록을 시도하다.")
    @Test
    @Transactional
    @WithMockCustomUser(tempValue = "forbidden")
    public void forbidden() throws Exception {
        //given
        final Long recruitmentNo = saveRecruitment.getRecruitmentNo();
        final String sido = "1";
        final String sigungu = "1111";
        final String details = "details";
        final String startDay = "05-26-2023";
        final String hourFormat = "AM";
        final String startTime = "10:00";
        final Integer progressTime = 2;
        final String organizationName = "organization";
        final Integer volunteerNum = 10;
        final String content = "content";
        ScheduleSave dto = new ScheduleSave(recruitmentNo, new AddressSave(sido, sigungu, details), startDay, hourFormat, startTime, progressTime,
                organizationName, volunteerNum, content);

        //when & then
        mockMvc.perform(post("/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(dto)))
                .andExpect(status().isForbidden())
                .andDo(print());
    }

    @DisplayName("수동 일정 등록간 입력값 조건을 위반하다.")
    @Test
    @Transactional
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void beanValidation() throws Exception {
        //given
        final Long recruitmentNo = saveRecruitment.getRecruitmentNo();
        final String sido = "1";
        final String sigungu = "1111";
        final String details = "details";
        final String startDay = "05-26-2023";
        final String hourFormat = "AM";
        final String startTime = "10:00";
        final Integer progressTime = 2;
        final String organizationName = "organization";
        final Integer volunteerNum = 100; //max 조건 위반
        final String content = "content";
        ScheduleSave dto = new ScheduleSave(recruitmentNo, new AddressSave(sido, sigungu, details), startDay, hourFormat, startTime, progressTime,
                organizationName, volunteerNum, content);

        //when & then
        mockMvc.perform(post("/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(dto)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @DisplayName("수동 일정 등록간 모집 인원은 봉사 팀원 최대 인원보다 많을 수 없다.")
    @Test
    @Transactional
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void exceedVolunteerNum() throws Exception {
        //given
        final Long recruitmentNo = saveRecruitment.getRecruitmentNo();
        final String sido = "1";
        final String sigungu = "1111";
        final String details = "details";
        final String startDay = "05-26-2023";
        final String hourFormat = "AM";
        final String startTime = "10:00";
        final Integer progressTime = 2;
        final String organizationName = "organization";
        final Integer volunteerNum = 20; //봉사 팀원 최대인원보다 작아야 된다.
        final String content = "content";
        ScheduleSave dto = new ScheduleSave(recruitmentNo, new AddressSave(sido, sigungu, details), startDay, hourFormat, startTime, progressTime,
                organizationName, volunteerNum, content);

        //when & then
        mockMvc.perform(post("/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(dto)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    private <T> String toJson(T data) throws JsonProcessingException {
        return objectMapper.writeValueAsString(data);
    }
}