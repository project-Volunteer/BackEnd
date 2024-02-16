package project.volunteer.document;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;
import project.volunteer.domain.recruitmentParticipation.api.dto.request.ParticipantAddParam;

public class RecruitmentParticipationControllerTest extends DocumentTest {

    @DisplayName("봉사 모집글 참여 신청에 성공하다.")
    @Test
    public void joinRecruitmentTeam() throws Exception {
        //given & when
        ResultActions result = mockMvc.perform(
                put("/recruitment/{recruitmentNo}/join", recruitment1.getRecruitmentNo())
                        .header(AUTHORIZATION_HEADER, loginUserAccessToken)
                        .contentType(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isOk())
                .andDo(print())
                .andDo(
                        restDocs.document(
                                requestHeaders(
                                        headerWithName(AUTHORIZATION_HEADER).description("JWT Access Token")
                                ),
                                pathParameters(
                                        parameterWithName("recruitmentNo").description("봉사 모집글 고유키 PK")
                                )
                        )
                );
    }

    @DisplayName("봉사 모집글 참여 신청 취소에 성공하다.")
    @Test
    public void cancelJoinRecruitmentTeam() throws Exception {
        //given & when
        ResultActions result = mockMvc.perform(
                put("/recruitment/{recruitmentNo}/cancel", recruitment1.getRecruitmentNo())
                        .header(AUTHORIZATION_HEADER, recruitmentTeamAccessToken2)
                        .contentType(MediaType.APPLICATION_JSON)
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
                                        parameterWithName("recruitmentNo").description("봉사 모집글 고유키 PK")
                                )
                        )
                );
    }

    @DisplayName("봉사 모집글 참여 신청 승인에 성공하다.")
    @Test
    public void approveJoinRecruitmentTeam() throws Exception {
        //given
        final ParticipantAddParam dto = new ParticipantAddParam(
                List.of(recruitmentParticipation2.getId(), recruitmentParticipation3.getId()));

        //when
        ResultActions result = mockMvc.perform(
                RestDocumentationRequestBuilders.put("/recruitment/{recruitmentNo}/approval",
                                recruitment1.getRecruitmentNo())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(AUTHORIZATION_HEADER, recruitmentOwnerAccessToken)
                        .content(toJson(dto))
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
                                        parameterWithName("recruitmentNo").description("봉사 모집글 고유키 PK")
                                ),
                                requestFields(
                                        fieldWithPath("recruitmentParticipationNos").type(JsonFieldType.ARRAY)
                                                .description("봉사 모집글 참여자 고유키 PK")
                                )
                        )
                );
    }

}
