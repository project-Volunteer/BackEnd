package project.volunteer.document;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static project.volunteer.document.restdocs.util.DocumentFormatGenerator.getDateFormat;
import static project.volunteer.document.restdocs.util.DocumentFormatGenerator.getTimeFormat;

import java.io.FileInputStream;
import java.io.IOException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.recruitment.domain.repeatPeriod.Day;
import project.volunteer.domain.recruitment.domain.repeatPeriod.Period;
import project.volunteer.domain.recruitment.domain.repeatPeriod.Week;
import project.volunteer.global.common.component.HourFormat;

public class RecruitmentControllerTest extends DocumentTest {

    @DisplayName("봉사 모집글 등록에 성공하다.")
    @Test
    void saveRecruitment() throws Exception {
        //given
        final MultiValueMap<String, String> request = new LinkedMultiValueMap<>();
        request.add("volunteeringCategory", VolunteeringCategory.ADMINSTRATION_ASSISTANCE.getId());
        request.add("organizationName", "unicef");
        request.add("isIssued", "true");
        request.add("volunteerType", VolunteerType.ALL.getId());
        request.add("maxParticipationNum", "10");
        request.add("volunteeringType", VolunteeringType.REG.getId());
        request.add("startDate", "01-10-2024");
        request.add("endDate", "02-30-2024");
        request.add("hourFormat", HourFormat.AM.getId());
        request.add("startTime", "12:30");
        request.add("progressTime", "10");
        request.add("period", Period.WEEK.getId());
        request.add("week", Week.NONE.getId());
        request.add("dayOfWeeks", Day.MON.getId());
        request.add("dayOfWeeks", Day.TUES.getId());
        request.add("title", "title");
        request.add("content", "content");
        request.add("isPublished", "true");
        request.add("address.sido", "111");
        request.add("address.sigungu", "11111");
        request.add("address.details", "detail");
        request.add("address.fullName", "111 11111 detail");
        request.add("address.latitude", "3.2");
        request.add("address.longitude", "3.2");
        request.add("picture.isStaticImage", "false");

        //when
        ResultActions result = mockMvc.perform(
                multipart("/recruitment")
                        .file(getMockMultipartFile())
                        .params(request)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                        .header(AUTHORIZATION_HEADER, recruitmentOwnerAccessToken)
        );

        //then
        result.andExpect(status().isCreated())
                .andDo(print())
                .andDo(
                        restDocs.document(
                                requestHeaders(
                                        headerWithName(AUTHORIZATION_HEADER).description("JWT Access Token")
                                ),
                                requestParts(
                                        partWithName("picture.uploadImage").optional()
                                                .attributes(key("constraints").value("정적 이미지일 경우 NULL 허용"))
                                                .description("첨부 이미지")
                                ),
                                requestParameters(
                                        parameterWithName("volunteeringCategory").description(
                                                "Code VolunteeringCategory 참조바람."),
                                        parameterWithName("organizationName").attributes(
                                                key("constraints").value("1이상 50이하")).description("기관 이름"),
                                        parameterWithName("address.sido").description("시/구 코드"),
                                        parameterWithName("address.sigungu").description("시/군/구 코드"),
                                        parameterWithName("address.details").description("상세주소"),
                                        parameterWithName("address.fullName").description("전체 주소 이름"),
                                        parameterWithName("address.latitude").description("위도"),
                                        parameterWithName("address.longitude").description("경도"),
                                        parameterWithName("isIssued").description("봉사 시간 인증 가능 여부"),
                                        parameterWithName("volunteerType").description("Code VolunteerType 참고바람."),
                                        parameterWithName("maxParticipationNum").attributes(
                                                key("constraints").value("1이상 9999이하")).description("봉사 모집 인원"),
                                        parameterWithName("volunteeringType").description("Code VolunteeringType 참고바람"),
                                        parameterWithName("startDate").attributes(getDateFormat())
                                                .description("봉사 모집 시작 날짜"),
                                        parameterWithName("endDate").attributes(getDateFormat())
                                                .description("봉사 모집 종료 날짜"),
                                        parameterWithName("hourFormat").description("Code HourFormat 참고바람."),
                                        parameterWithName("startTime").attributes(getTimeFormat())
                                                .description("정기 봉사 일정 시작 시간"),
                                        parameterWithName("progressTime").attributes(
                                                key("constraints").value("1이상 24이하")).description("정기 봉사 일정 진행 시간"),
                                        parameterWithName("period").optional()
                                                .attributes(key("constraints").value("비정기일 경우 NONE"))
                                                .description("Code Period 참고바람."),
                                        parameterWithName("week").optional()
                                                .attributes(key("constraints").value("비정기 혹은 Period가 매주일 경우 NONE"))
                                                .description("Code Week 참고바람."),
                                        parameterWithName("dayOfWeeks").optional()
                                                .attributes(key("constraints").value("비정기일 경우 빈 배열"))
                                                .description("Code Day 참고바람, 다중 값 허용(배열)"),
                                        parameterWithName("picture.isStaticImage").description("정적/업로드 이미지 구분"),
                                        parameterWithName("title").attributes(key("constraints").value("1이상 255이하"))
                                                .description("봉사 모집글 제목"),
                                        parameterWithName("content").attributes(key("constraints").value("1이상 255이하"))
                                                .description("봉사 모집글 본문"),
                                        parameterWithName("isPublished").description("임시 저장 유무")
                                ),
                                responseFields(
                                        fieldWithPath("no").type(JsonFieldType.NUMBER).description("봉사 모집글 고유키 PK")
                                )
                        )
                );
    }

    @DisplayName("정기 모집글 삭제에 성공하다.")
    @Test
    public void deleteRecruitment() throws Exception {
        //given & when
        ResultActions result = mockMvc.perform(
                delete("/recruitment/{recruitmentNo}", recruitment.getRecruitmentNo())
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
                                        parameterWithName("recruitmentNo").description("봉사 모집글 고유키 PK")
                                )
                        )
                );
    }

    private MockMultipartFile getMockMultipartFile() throws IOException {
        return new MockMultipartFile(
                "picture.uploadImage", "file.PNG", "image/jpg",
                new FileInputStream("src/main/resources/static/test/file.PNG"));
    }

}
