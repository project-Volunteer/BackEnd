package project.volunteer.document;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static project.volunteer.document.restdocs.util.DocumentFormatGenerator.getDateFormat;
import static project.volunteer.document.restdocs.util.DocumentFormatGenerator.getTimeFormat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;
import project.volunteer.domain.sehedule.api.dto.request.AddressRequest;
import project.volunteer.domain.sehedule.api.dto.request.ScheduleUpsertRequest;

public class ScheduleControllerTest extends DocumentTest {

    @DisplayName("일정 등록에 성공하다.")
    @Test
    public void saveSchedule() throws Exception {
        //given
        final ScheduleUpsertRequest request = new ScheduleUpsertRequest(
                new AddressRequest("1", "1111", "1111", "1111"), "02-10-2024", "AM", "10:00", 2,
                "unicef", 100, "content");

        //when
        ResultActions result = mockMvc.perform(
                RestDocumentationRequestBuilders.post("/recruitment/{recruitmentNo}/schedule",
                                recruitment.getRecruitmentNo())
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
                new AddressRequest("1", "1111", "1111", "1111"), "02-15-2024", "AM", "10:00", 2,
                "unicef", 50, "content");

        //when
        ResultActions result = mockMvc.perform(
                RestDocumentationRequestBuilders.put("/recruitment/{recruitmentNo}/schedule/{scheduleNo}",
                                recruitment.getRecruitmentNo(), schedule.getScheduleNo())
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


}
