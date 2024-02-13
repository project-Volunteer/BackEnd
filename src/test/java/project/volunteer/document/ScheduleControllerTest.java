package project.volunteer.document;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static project.volunteer.document.restdocs.util.DocumentFormatGenerator.getDateFormat;
import static project.volunteer.document.restdocs.util.DocumentFormatGenerator.getTimeFormat;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;
import project.volunteer.domain.sehedule.api.dto.request.ScheduleAddressRequest;
import project.volunteer.domain.sehedule.api.dto.request.ScheduleUpsertRequest;
import project.volunteer.global.common.dto.StateResult;

public class ScheduleControllerTest extends DocumentTest {

    @DisplayName("일정 등록에 성공하다.")
    @Test
    public void saveSchedule() throws Exception {
        //given
        final ScheduleUpsertRequest request = new ScheduleUpsertRequest(
                new ScheduleAddressRequest("1", "1111", "1111", "1111"), "02-10-2024", "AM", "10:00", 2,
                "unicef", 100, "content");

        //when
        ResultActions result = mockMvc.perform(
                RestDocumentationRequestBuilders.post("/recruitment/{recruitmentNo}/schedule",
                                recruitment1.getRecruitmentNo())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(AUTHORIZATION_HEADER, recruitmentOwnerAccessToken)
                        .content(toJson(request))
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
                                        fieldWithPath("address.sido").type(JsonFieldType.STRING)
                                                .attributes(key("constraints").value("1이상 5이하")).description("시/도 코드"),
                                        fieldWithPath("address.sigungu").type(JsonFieldType.STRING)
                                                .attributes(key("constraints").value("1이상 10이하"))
                                                .description("시/군/구/ 코드"),
                                        fieldWithPath("address.details").type(JsonFieldType.STRING)
                                                .attributes(key("constraints").value("1이상 50이하")).description("상세주소"),
                                        fieldWithPath("address.fullName").type(JsonFieldType.STRING)
                                                .attributes(key("constraints").value("1이상 255이하"))
                                                .description("전체 주소 이름"),
                                        fieldWithPath("startDay").type(JsonFieldType.STRING).attributes(getDateFormat())
                                                .description("봉사 일정 시작날짜"),
                                        fieldWithPath("hourFormat").type(JsonFieldType.STRING)
                                                .description("Code HourFormat 참고바람."),
                                        fieldWithPath("startTime").type(JsonFieldType.STRING)
                                                .attributes(getTimeFormat()).description("봉사 일정 시작시간"),
                                        fieldWithPath("progressTime").type(JsonFieldType.NUMBER)
                                                .attributes(key("constraints").value("1이상 24이하"))
                                                .description("봉사 일정 진행시간"),
                                        fieldWithPath("organizationName").type(JsonFieldType.STRING)
                                                .description("봉사 기관이름"),
                                        fieldWithPath("volunteerNum").type(JsonFieldType.NUMBER).attributes(
                                                        key("constraints").value("1이상 9999이하 & 승인된 봉사 팀원 총 인원보다 많을 수 없음."))
                                                .description("봉사 일정 참여가능 인원"),
                                        fieldWithPath("content").type(JsonFieldType.STRING).optional()
                                                .attributes(key("constraints").value("50이하"))
                                                .description("봉사 일정 관련 간단 문구")
                                )
                        )
                );
    }

    @DisplayName("일정 수정에 성공하다.")
    @Test
    public void editSchedule() throws Exception {
        //given
        final ScheduleUpsertRequest request = new ScheduleUpsertRequest(
                new ScheduleAddressRequest("1", "1111", "1111", "1111"), "02-15-2024", "AM", "10:00", 2,
                "unicef", 50, "content");

        //when
        ResultActions result = mockMvc.perform(
                RestDocumentationRequestBuilders.put("/recruitment/{recruitmentNo}/schedule/{scheduleNo}",
                                recruitment1.getRecruitmentNo(), schedule1.getScheduleNo())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(AUTHORIZATION_HEADER, recruitmentOwnerAccessToken)
                        .content(toJson(request))
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
                                ),
                                requestFields(
                                        fieldWithPath("address.sido").type(JsonFieldType.STRING)
                                                .attributes(key("constraints").value("1이상 5이하"))
                                                .description("수정할 시/도 코드"),
                                        fieldWithPath("address.sigungu").type(JsonFieldType.STRING)
                                                .attributes(key("constraints").value("1이상 10이하"))
                                                .description("수정할 시/군/구/ 코드"),
                                        fieldWithPath("address.details").type(JsonFieldType.STRING)
                                                .attributes(key("constraints").value("1이상 50이하"))
                                                .description("수정할 상세주소"),
                                        fieldWithPath("address.fullName").type(JsonFieldType.STRING)
                                                .attributes(key("constraints").value("1이상 255이하"))
                                                .description("수정할 전체 주소 이름"),
                                        fieldWithPath("startDay").type(JsonFieldType.STRING)
                                                .attributes(key("constraints").value("mm-dd-yyyy"))
                                                .description("수정할 봉사 일정 시작날짜"),
                                        fieldWithPath("hourFormat").type(JsonFieldType.STRING)
                                                .description("Code HourFormat 참고바람."),
                                        fieldWithPath("startTime").type(JsonFieldType.STRING)
                                                .attributes(key("constraints").value("HH:mm"))
                                                .description("수정할 봉사 일정 시작시간"),
                                        fieldWithPath("progressTime").type(JsonFieldType.NUMBER)
                                                .attributes(key("constraints").value("1이상 24이하"))
                                                .description("수정할 봉사 일정 진행시간"),
                                        fieldWithPath("organizationName").type(JsonFieldType.STRING)
                                                .description("봉사 기관이름"),
                                        fieldWithPath("volunteerNum").type(JsonFieldType.NUMBER)
                                                .attributes(key("constraints").value(
                                                        "1이상 9999이하 & 승인된 봉사 팀원 총 인원보다 많을 수 없음. & 현재 일정 참여 총 인원보다 적을 수 없음."))
                                                .description("수정할 봉사 일정 참여가능 인원"),
                                        fieldWithPath("content").type(JsonFieldType.STRING).optional()
                                                .attributes(key("constraints").value("50이하"))
                                                .description("수정할 봉사 일정 관련 간단 문구")
                                )
                        )
                );
    }

    @DisplayName("일정 삭제에 성공하다.")
    @Test
    public void deleteSchedule() throws Exception {
        //given & when
        ResultActions result = mockMvc.perform(
                RestDocumentationRequestBuilders.delete("/recruitment/{recruitmentNo}/schedule/{scheduleNo}",
                                recruitment1.getRecruitmentNo(), schedule1.getScheduleNo())
                        .header(AUTHORIZATION_HEADER, recruitmentOwnerAccessToken)
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

    @DisplayName("가장 가까운 일정 상세 조회에 성공하다.")
    @Test
    public void detailClosestSchedule() throws Exception {
        // given
        given(clock.instant()).willReturn(Instant.parse("2024-02-01T10:00:00Z"));

        // when
        ResultActions result = mockMvc.perform(
                RestDocumentationRequestBuilders.get("/recruitment/{recruitmentNo}/schedule",
                                recruitment1.getRecruitmentNo())
                        .header(AUTHORIZATION_HEADER, recruitmentTeamAccessToken)
        );

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("activeVolunteerNum").value(0))
                .andExpect(jsonPath("state").value(StateResult.AVAILABLE.name()))
                .andDo(print())
                .andDo(
                        restDocs.document(
                                requestHeaders(
                                        headerWithName(AUTHORIZATION_HEADER).description("JWT Access Token")
                                ),
                                pathParameters(
                                        parameterWithName("recruitmentNo").description("봉사 모집글 고유키 PK")
                                ),
                                responseFields(
                                        fieldWithPath("no").type(JsonFieldType.NUMBER).description("봉사 일정 고유키 PK"),
                                        fieldWithPath("address.sido").type(JsonFieldType.STRING).description("시/구 코드"),
                                        fieldWithPath("address.sigungu").type(JsonFieldType.STRING)
                                                .description("시/군/구/ 코드"),
                                        fieldWithPath("address.details").type(JsonFieldType.STRING).description("상세주소"),
                                        fieldWithPath("address.fullName").type(JsonFieldType.STRING)
                                                .description("전체 주소 이름"),
                                        fieldWithPath("startDate").type(JsonFieldType.STRING)
                                                .attributes(getDateFormat()).description("봉사 일정 시작날짜"),
                                        fieldWithPath("startTime").type(JsonFieldType.STRING)
                                                .attributes(getTimeFormat()).description("봉사 일정 시작시간"),
                                        fieldWithPath("hourFormat").type(JsonFieldType.STRING)
                                                .description("Code HourFormat 참고바람."),
                                        fieldWithPath("progressTime").type(JsonFieldType.NUMBER)
                                                .description("봉사 일정 진행시간"),
                                        fieldWithPath("volunteerNum").type(JsonFieldType.NUMBER)
                                                .description("봉사 일정 참여 가능 인원"),
                                        fieldWithPath("content").type(JsonFieldType.STRING)
                                                .description("봉사 일정 관련 간단 문구"),
                                        fieldWithPath("activeVolunteerNum").type(JsonFieldType.NUMBER)
                                                .description("현재 봉사 일정 참여 인원"),
                                        fieldWithPath("state").type(JsonFieldType.STRING)
                                                .description("Code ClientState 참고바람."),
                                        fieldWithPath("hasData").type(JsonFieldType.BOOLEAN)
                                                .description("응답 데이터 값의 존재 여부, 참여 가능한 가장 가까운 일정이 없는 경우 false 입니다.")
                                )
                        )
                );
    }

    @DisplayName("2024년 2월 캘린더에 존재하는 일정 리스트 조회에 성공하다.")
    @Test
    public void CalendarScheduleList() throws Exception {
        // given
        final String searchYear = "2024";
        final String searchMonth = "2";

        // when
        ResultActions result = mockMvc.perform(
                RestDocumentationRequestBuilders.get("/recruitment/{recruitmentNo}/calendar",
                                recruitment1.getRecruitmentNo())
                        .header(AUTHORIZATION_HEADER, recruitmentTeamAccessToken)
                        .queryParam("year", searchYear)
                        .queryParam("mon", searchMonth)
        );

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.scheduleList[0].no").value(schedule1.getScheduleNo()))
                .andExpect(jsonPath("$.scheduleList[1].no").value(schedule3.getScheduleNo()))
                .andExpect(jsonPath("$.scheduleList[2].no").value(schedule2.getScheduleNo()))
                .andDo(print())
                .andDo(
                        restDocs.document(
                                requestHeaders(
                                        headerWithName(AUTHORIZATION_HEADER).description("JWT Access Token")
                                ),
                                pathParameters(
                                        parameterWithName("recruitmentNo").description("봉사 모집글 고유키 PK")
                                ),
                                requestParameters(
                                        parameterWithName("year").description("년도"),
                                        parameterWithName("mon").description("월")
                                ),
                                responseFields(
                                        fieldWithPath("scheduleList[].no").type(JsonFieldType.NUMBER)
                                                .description("봉사 일정 고유키 PK"),
                                        fieldWithPath("scheduleList[].day").type(JsonFieldType.STRING)
                                                .attributes(getDateFormat()).description("봉사 일정 날짜")
                                )
                        )
                );
    }

    @Test
    @DisplayName("일정 상세 조회에 성공하다.")
    public void detailSchedule() throws Exception {
        // given && when
        given(clock.instant()).willReturn(Instant.parse("2024-02-09T10:00:00Z"));
        ResultActions result = mockMvc.perform(
                RestDocumentationRequestBuilders.get("/recruitment/{recruitmentNo}/calendar/{scheduleNo}",
                                recruitment1.getRecruitmentNo(), schedule1.getScheduleNo())
                        .header(AUTHORIZATION_HEADER, recruitmentTeamAccessToken)
        );

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("activeVolunteerNum").value(0))
                .andExpect(jsonPath("state").value(StateResult.AVAILABLE.name()))
                .andDo(print())
                .andDo(
                        restDocs.document(
                                requestHeaders(
                                        headerWithName(AUTHORIZATION_HEADER).description("JWT Access Token")
                                ),
                                pathParameters(
                                        parameterWithName("recruitmentNo").description("봉사 모집글 고유키 PK"),
                                        parameterWithName("scheduleNo").description("봉사 일정 고유키 PK")
                                ),
                                responseFields(
                                        fieldWithPath("no").type(JsonFieldType.NUMBER).description("봉사 일정 고유키 PK"),
                                        fieldWithPath("address.sido").type(JsonFieldType.STRING).description("시/구 코드"),
                                        fieldWithPath("address.sigungu").type(JsonFieldType.STRING)
                                                .description("시/군/구/ 코드"),
                                        fieldWithPath("address.details").type(JsonFieldType.STRING).description("상세주소"),
                                        fieldWithPath("address.fullName").type(JsonFieldType.STRING)
                                                .description("전체 주소 이름"),
                                        fieldWithPath("startDate").type(JsonFieldType.STRING)
                                                .attributes(getDateFormat()).description("봉사 일정 시작날짜"),
                                        fieldWithPath("startTime").type(JsonFieldType.STRING)
                                                .attributes(getTimeFormat()).description("봉사 일정 시작시간"),
                                        fieldWithPath("hourFormat").type(JsonFieldType.STRING)
                                                .description("Code HourFormat 참고바람."),
                                        fieldWithPath("progressTime").type(JsonFieldType.NUMBER)
                                                .description("봉사 일정 진행시간"),
                                        fieldWithPath("volunteerNum").type(JsonFieldType.NUMBER)
                                                .description("봉사 일정 참여 가능 인원"),
                                        fieldWithPath("content").type(JsonFieldType.STRING)
                                                .description("봉사 일정 관련 간단 문구"),
                                        fieldWithPath("activeVolunteerNum").type(JsonFieldType.NUMBER)
                                                .description("현재 봉사 일정 참여 인원"),
                                        fieldWithPath("state").type(JsonFieldType.STRING)
                                                .description("Code ClientState 참고바람."),
                                        fieldWithPath("hasData").type(JsonFieldType.BOOLEAN)
                                                .description("응답 데이터 값의 존재여부, 정상 응답일 경우 항상 true입니다.")
                                )
                        )
                );
    }

}
