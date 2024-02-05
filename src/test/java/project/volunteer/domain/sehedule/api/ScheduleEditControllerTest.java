package project.volunteer.domain.sehedule.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
import project.volunteer.domain.participation.dao.ParticipantRepository;
import project.volunteer.domain.participation.domain.Participant;
import project.volunteer.domain.recruitment.dao.RecruitmentRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.scheduleParticipation.repository.ScheduleParticipationRepository;
import project.volunteer.domain.scheduleParticipation.domain.ScheduleParticipation;
import project.volunteer.domain.sehedule.api.dto.request.AddressRequest;
import project.volunteer.domain.sehedule.api.dto.request.ScheduleUpsertRequest;
import project.volunteer.domain.sehedule.repository.ScheduleRepository;
import project.volunteer.domain.sehedule.domain.Schedule;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.domain.Gender;
import project.volunteer.domain.user.domain.Role;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.*;
import project.volunteer.global.test.WithMockCustomUser;
import project.volunteer.document.restdocs.config.RestDocsConfiguration;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import(RestDocsConfiguration.class)
class ScheduleEditControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @PersistenceContext EntityManager em;
    @Autowired UserRepository userRepository;
    @Autowired RecruitmentRepository recruitmentRepository;
    @Autowired ScheduleRepository scheduleRepository;
    @Autowired ParticipantRepository participantRepository;
    @Autowired ScheduleParticipationRepository scheduleParticipationRepository;
    @Autowired RestDocumentationResultHandler restDocs;

    final String AUTHORIZATION_HEADER = "accessToken";
    Schedule saveSchedule;
    Recruitment saveRecruitment;
    private void clear() {
        em.flush();
        em.clear();
    }
    @BeforeEach
    private void setUp() {
        //작성자 저장
        User writer = User.createUser("sctfe1234", "sctfe1234", "sctfe1234", "sctfe1234", Gender.M, LocalDate.now(), "picture",
                true, true, true, Role.USER, "kakao", "sctfe1234", null);
        userRepository.save(writer);

        //Embedded 값 세팅
        Address address = Address.createAddress("1", "111", "test", "fullName");
        Timetable timetable = Timetable.createTimetable(LocalDate.now(), LocalDate.now(), HourFormat.AM, LocalTime.now(), 10);
        Coordinate coordinate = Coordinate.createCoordinate(3.2F, 3.2F);

        //봉사 모집글 저장
        saveRecruitment =
                Recruitment.createRecruitment("test", "test", VolunteeringCategory.EDUCATION, VolunteeringType.IRREG,
                        VolunteerType.TEENAGER, 10, true, "test", address, coordinate, timetable, true);
        saveRecruitment.setWriter(writer);
        recruitmentRepository.save(saveRecruitment);

        //일정 등록
        saveSchedule = Schedule.create(saveRecruitment, timetable, "test", "organizaion", address, 8);
        scheduleRepository.save(saveSchedule);

        //봉사 팀원 및 일정 참여자 등록
        for(int i=0; i<5;i++){
            User user = User.createUser("sctfe" + i, "sctfe" + i, "sctfe" + i, "sctfe" + i, Gender.M, LocalDate.now(),
                    "picture", true, true, true, Role.USER, "kakao", "sctfe" + i, null);
            userRepository.save(user);

            Participant participant = Participant.createParticipant(saveRecruitment, user, ParticipantState.JOIN_APPROVAL);
            participantRepository.save(participant);

            ScheduleParticipation scheduleParticipation =
                    ScheduleParticipation.createScheduleParticipation(saveSchedule, participant, ParticipantState.PARTICIPATING);
            scheduleParticipationRepository.save(scheduleParticipation);
        }

        clear();
    }

    @Disabled
    @DisplayName("방장이 아닌 사용자가 일정 수정을 시도하다.")
    @Test
    @Transactional
    @WithMockCustomUser(tempValue = "sctfe0")
    public void forbidden() throws Exception {
        //given
        final Long scheduleNo = saveSchedule.getScheduleNo();
        final String sido = "1";
        final String sigungu = "1111";
        final String details = "details";
        final String fullName = "fullName";
        final String startDay = "05-26-2023";
        final String hourFormat = "AM";
        final String startTime = "10:00";
        final Integer progressTime = 2;
        final String organizationName = "organization";
        final Integer volunteerNum = 6;
        final String content = "content";
        ScheduleUpsertRequest dto = new ScheduleUpsertRequest(new AddressRequest(sido, sigungu, details, fullName), startDay, hourFormat, startTime, progressTime,
                organizationName, volunteerNum, content);

        //when & then
        mockMvc.perform(put("/recruitment/{recruitmentNo}/schedule/{scheduleNo}", saveRecruitment.getRecruitmentNo(), scheduleNo)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(dto)))
                .andExpect(status().isForbidden())
                .andDo(print());
    }

    @Disabled
    @DisplayName("봉사 일정 수정간 모집 인원은 현재 일정 참여자 수보다 적을 수 없다.")
    @Test
    @Transactional
    @WithUserDetails(value = "sctfe1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void exceedVolunteerNum() throws Exception {
        //given
        final Long scheduleNo = saveSchedule.getScheduleNo();
        final String sido = "1";
        final String sigungu = "1111";
        final String details = "details";
        final String fullName = "fullName";
        final String startDay = "05-26-2023";
        final String hourFormat = "AM";
        final String startTime = "10:00";
        final Integer progressTime = 2;
        final String organizationName = "organization";
        final Integer volunteerNum = 3; //현재 일정에 참여중인 인원수(5명) 보다 작을 수 없다.!
        final String content = "content";
        ScheduleUpsertRequest dto = new ScheduleUpsertRequest(new AddressRequest(sido, sigungu, details, fullName), startDay, hourFormat, startTime, progressTime,
                organizationName, volunteerNum, content);

        //when & then
        mockMvc.perform(put("/recruitment/{recruitmentNo}/schedule/{scheduleNo}", saveRecruitment.getRecruitmentNo(), scheduleNo)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(dto)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Disabled
    @DisplayName("잘못된 MediaType 으로 일정 수정 API를 요청하다.")
    @Test
    @Transactional
    @WithUserDetails(value = "sctfe1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void invalidMediaType() throws Exception {
        //given
        final Long scheduleNo = saveSchedule.getScheduleNo();
        final String sido = "1";
        final String sigungu = "1111";
        final String details = "details";
        final String fullName = "fullName";
        final String startDay = "05-26-2023";
        final String hourFormat = "AM";
        final String startTime = "10:00";
        final Integer progressTime = 2;
        final String organizationName = "organization";
        final Integer volunteerNum = 6;
        final String content = "content";
        ScheduleUpsertRequest dto = new ScheduleUpsertRequest(new AddressRequest(sido, sigungu, details, fullName), startDay, hourFormat, startTime, progressTime,
                organizationName, volunteerNum, content);

        //when & then
        mockMvc.perform(put("/recruitment/{recruitmentNo}/schedule/{scheduleNo}", saveRecruitment.getRecruitmentNo(), scheduleNo)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE) //잘못된 Media Type!
                        .content(toJson(dto)))
                .andExpect(status().isUnsupportedMediaType())
                .andDo(print());
    }

    @DisplayName("봉사 일정 삭제에 성공하다.")
    @Test
    @Transactional
    @WithUserDetails(value = "sctfe1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void deleteSchedule() throws Exception {
        //given & when
        ResultActions result = mockMvc.perform(
                RestDocumentationRequestBuilders.delete("/recruitment/{recruitmentNo}/schedule/{scheduleNo}", saveRecruitment.getRecruitmentNo(), saveSchedule.getScheduleNo())
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
                                pathParameters(
                                        parameterWithName("recruitmentNo").description("봉사 모집글 고유키 PK"),
                                        parameterWithName("scheduleNo").description("봉사 일정 고유키 PK")
                                )
                        )
                );
    }

    private <T> String toJson(T data) throws JsonProcessingException {
        return objectMapper.writeValueAsString(data);
    }
}