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
import project.volunteer.domain.recruitment.dao.RecruitmentRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.sehedule.api.dto.request.AddressRequest;
import project.volunteer.domain.sehedule.api.dto.request.ScheduleUpsertRequest;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.domain.Gender;
import project.volunteer.domain.user.domain.Role;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.*;
import project.volunteer.global.test.WithMockCustomUser;
import project.volunteer.restdocs.document.config.RestDocsConfiguration;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static project.volunteer.restdocs.document.util.DocumentFormatGenerator.getDateFormat;
import static project.volunteer.restdocs.document.util.DocumentFormatGenerator.getTimeFormat;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import(RestDocsConfiguration.class)
class ScheduleWriteControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;
    @Autowired RecruitmentRepository recruitmentRepository;
    @Autowired ObjectMapper objectMapper;
    @Autowired RestDocumentationResultHandler restDocs;

    final String AUTHORIZATION_HEADER = "accessToken";
    Recruitment saveRecruitment;
    @BeforeEach
    public void setup(){
        //작성자 저장
        User writer = User.createUser("sctfw1234", "sctfw1234", "sctfw1234", "sctfw1234", Gender.M, LocalDate.now(), "picture",
                true, true, true, Role.USER, "kakao", "sctfw1234", null);
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
    }

    @DisplayName("수동 일정 등록에 성공하다.")
    @Test
    @Transactional
    @WithUserDetails(value = "sctfw1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void saveSchedule() throws Exception {
        //given
        final Long recruitmentNo = saveRecruitment.getRecruitmentNo();
        final String sido = "1";
        final String sigungu = "1111";
        final String details = "details";
        final String fullName = "fullName";
        final String startDay = "05-26-2023";
        final String hourFormat = "AM";
        final String startTime = "10:00";
        final Integer progressTime = 2;
        final String organizationName = "organization";
        final Integer volunteerNum = 10;
        final String content = "content";
        ScheduleUpsertRequest dto = new ScheduleUpsertRequest(new AddressRequest(sido, sigungu, details, fullName), startDay, hourFormat, startTime, progressTime,
                organizationName, volunteerNum, content);

        //when
        ResultActions result = mockMvc.perform(RestDocumentationRequestBuilders.post("/recruitment/{recruitmentNo}/schedule", recruitmentNo)
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION_HEADER, "access Token")
                .content(toJson(dto))
        );

        //then
        result.andExpect(status().isCreated())
                .andDo(print())
                .andDo(
                        restDocs.document(
                                requestHeaders(
                                        headerWithName(AUTHORIZATION_HEADER).description("JWT Access Token")
                                ),
                                pathParameters(
                                        parameterWithName("recruitmentNo").description("봉사 모집글 고유키 PK")
                                ),
                                requestFields(
                                        fieldWithPath("address.sido").type(JsonFieldType.STRING).attributes(key("constraints").value("1이상 5이하")).description("시/도 코드"),
                                        fieldWithPath("address.sigungu").type(JsonFieldType.STRING).attributes(key("constraints").value("1이상 10이하")).description("시/군/구/ 코드"),
                                        fieldWithPath("address.details").type(JsonFieldType.STRING).attributes(key("constraints").value("1이상 50이하")).description("상세주소"),
                                        fieldWithPath("address.fullName").type(JsonFieldType.STRING).attributes(key("constraints").value("1이상 255이하")).description("전체 주소 이름"),
                                        fieldWithPath("startDay").type(JsonFieldType.STRING).attributes(getDateFormat()).description("봉사 일정 시작날짜"),
                                        fieldWithPath("hourFormat").type(JsonFieldType.STRING).description("Code HourFormat 참고바람."),
                                        fieldWithPath("startTime").type(JsonFieldType.STRING).attributes(getTimeFormat()).description("봉사 일정 시작시간"),
                                        fieldWithPath("progressTime").type(JsonFieldType.NUMBER).attributes(key("constraints").value("1이상 24이하")).description("봉사 일정 진행시간"),
                                        fieldWithPath("organizationName").type(JsonFieldType.STRING).description("봉사 기관이름"),
                                        fieldWithPath("volunteerNum").type(JsonFieldType.NUMBER).attributes(key("constraints").value("1이상 50이하 & 승인된 봉사 팀원 총 인원보다 많을 수 없음.")).description("봉사 일정 참여가능 인원"),
                                        fieldWithPath("content").type(JsonFieldType.STRING).optional().attributes(key("constraints").value("50이하")).description("봉사 일정 관련 간단 문구")
                                )
                        )
                );
    }

    @Disabled
    @DisplayName("방장이 아닌 사용자가 수동 일정 등록을 시도하다.")
    @Test
    @Transactional
    @WithMockCustomUser(tempValue = "sctfw_forbidden")
    public void forbidden() throws Exception {
        //given
        final Long recruitmentNo = saveRecruitment.getRecruitmentNo();
        final String sido = "1";
        final String sigungu = "1111";
        final String details = "details";
        final String fullName = "fullName";
        final String startDay = "05-26-2023";
        final String hourFormat = "AM";
        final String startTime = "10:00";
        final Integer progressTime = 2;
        final String organizationName = "organization";
        final Integer volunteerNum = 10;
        final String content = "content";
        ScheduleUpsertRequest dto = new ScheduleUpsertRequest(new AddressRequest(sido, sigungu, details, fullName), startDay, hourFormat, startTime, progressTime,
                organizationName, volunteerNum, content);

        //when & then
        mockMvc.perform(post("/recruitment/{recruitmentNo}/schedule", recruitmentNo)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(dto)))
                .andExpect(status().isForbidden())
                .andDo(print());
    }

    @Disabled
    @DisplayName("수동 일정 등록간 입력값 조건을 위반하다.")
    @Test
    @Transactional
    @WithUserDetails(value = "sctfw1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void beanValidation() throws Exception {
        //given
        final Long recruitmentNo = saveRecruitment.getRecruitmentNo();
        final String sido = "1";
        final String sigungu = "1111";
        final String details = "details";
        final String fullName = "fullName";
        final String startDay = "05-26-2023";
        final String hourFormat = "AM";
        final String startTime = "10:00";
        final Integer progressTime = 2;
        final String organizationName = "organization";
        final Integer volunteerNum = 100; //max 조건 위반
        final String content = "content";
        ScheduleUpsertRequest dto = new ScheduleUpsertRequest(new AddressRequest(sido, sigungu, details, fullName), startDay, hourFormat, startTime, progressTime,
                organizationName, volunteerNum, content);

        //when & then
        mockMvc.perform(post("/recruitment/{recruitmentNo}/schedule", recruitmentNo)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(dto)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Disabled
    @DisplayName("수동 일정 등록간 모집 인원은 봉사 팀원 최대 인원보다 많을 수 없다.")
    @Test
    @Transactional
    @WithUserDetails(value = "sctfw1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void exceedVolunteerNum() throws Exception {
        //given
        final Long recruitmentNo = saveRecruitment.getRecruitmentNo();
        final String sido = "1";
        final String sigungu = "1111";
        final String details = "details";
        final String fullName = "fullName";
        final String startDay = "05-26-2023";
        final String hourFormat = "AM";
        final String startTime = "10:00";
        final Integer progressTime = 2;
        final String organizationName = "organization";
        final Integer volunteerNum = 20; //봉사 팀원 최대인원보다 작아야 된다.
        final String content = "content";
        ScheduleUpsertRequest dto = new ScheduleUpsertRequest(new AddressRequest(sido, sigungu, details, fullName), startDay, hourFormat, startTime, progressTime,
                organizationName, volunteerNum, content);

        //when & then
        mockMvc.perform(post("/recruitment/{recruitmentNo}/schedule", recruitmentNo)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(dto)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    private <T> String toJson(T data) throws JsonProcessingException {
        return objectMapper.writeValueAsString(data);
    }
}