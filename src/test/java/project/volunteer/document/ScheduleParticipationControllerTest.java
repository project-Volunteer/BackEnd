package project.volunteer.document;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;
import project.volunteer.domain.scheduleParticipation.api.dto.CancellationApprovalRequest;
import project.volunteer.domain.scheduleParticipation.api.dto.ParticipationCompletionApproveRequest;

public class ScheduleParticipationControllerTest extends DocumentTest {

    @DisplayName("일정 참여에 성공하다.")
    @Test
    void participateSchedule() throws Exception {
        //given & when
        given(clock.instant()).willReturn(Instant.parse("2024-02-09T10:00:00Z"));

        ResultActions result = mockMvc.perform(
                put("/recruitment/{recruitmentNo}/schedule/{scheduleNo}/join", recruitment1.getRecruitmentNo(),
                        schedule1.getScheduleNo())
                        .header(AUTHORIZATION_HEADER, recruitmentTeamAccessToken1));

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

    @DisplayName("일정 참여 취소 요청에 성공하다.")
    @Test
    void cancelParticipationSchedule() throws Exception {
        //given & when
        given(clock.instant()).willReturn(Instant.parse("2024-02-09T10:00:00Z"));

        ResultActions result = mockMvc.perform(
                put("/recruitment/{recruitmentNo}/schedule/{scheduleNo}/cancel", recruitment1.getRecruitmentNo(),
                        schedule1.getScheduleNo())
                        .header(AUTHORIZATION_HEADER, recruitmentTeamAccessToken4));

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

    @DisplayName("일정 참여 취소 요청 승인에 성공하다.")
    @Test
    void approveCancellationSchedule() throws Exception {
        //given & when
        given(clock.instant()).willReturn(Instant.parse("2024-02-09T10:00:00Z"));

        final CancellationApprovalRequest request = new CancellationApprovalRequest(
                List.of(scheduleParticipation5.getId()));

        ResultActions result = mockMvc.perform(
                put("/recruitment/{recruitmentNo}/schedule/{scheduleNo}/cancelling", recruitment1.getRecruitmentNo(),
                        schedule1.getScheduleNo())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(AUTHORIZATION_HEADER, recruitmentOwnerAccessToken)
                        .content(toJson(request)));

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
                                        fieldWithPath("scheduleParticipationNos").type(JsonFieldType.ARRAY)
                                                .description("일정 참여자 고유키 PK")
                                )
                        )
                );
    }

    @DisplayName("일정 참여 완료 승인에 성공하다.")
    @Test
    void approveParticipationCompletion() throws Exception {
        //given & when
        final ParticipationCompletionApproveRequest request = new ParticipationCompletionApproveRequest(
                List.of(scheduleParticipation6.getId()));

        ResultActions result = mockMvc.perform(
                put("/recruitment/{recruitmentNo}/schedule/{scheduleNo}/complete", recruitment1.getRecruitmentNo(),
                        schedule1.getScheduleNo())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(AUTHORIZATION_HEADER, recruitmentOwnerAccessToken)
                        .content(toJson(request)));

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
                                        fieldWithPath("scheduleParticipationNos").type(JsonFieldType.ARRAY)
                                                .description("일정 참여자 고유키 PK")
                                )
                        )
                );
    }

}
