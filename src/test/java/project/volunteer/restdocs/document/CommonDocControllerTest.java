package project.volunteer.restdocs.document;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.PayloadSubsectionExtractor;
import org.springframework.test.web.servlet.MockMvc;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.recruitment.domain.Day;
import project.volunteer.domain.recruitment.domain.Period;
import project.volunteer.domain.recruitment.domain.Week;
import project.volunteer.global.common.component.HourFormat;
import project.volunteer.global.common.converter.CodeCommonType;
import project.volunteer.global.common.dto.StateResponse;
import project.volunteer.restdocs.document.util.CustomResponseFieldsSnippet;

import java.util.Arrays;
import java.util.Map;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.snippet.Attributes.attributes;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
public class CommonDocControllerTest {
    @Autowired private MockMvc mockMvc;
    @Test
    public void commons() throws Exception {
        mockMvc.perform(get("/docs/enums"))
                .andExpect(status().isOk())
                .andDo(document("enums",
                        customResponseFields("custom-response", beneathPath("data.hourFormat").withSubsectionId("hourFormat"),
                                attributes(key("title").value("시간포멧")),
                                enumConvertFieldDescriptor(HourFormat.values())
                        ),
                        customResponseFields("custom-response", beneathPath("data.clientState").withSubsectionId("clientState"),
                                attributes(key("title").value("클라이언트 신청 상태")),
                                enumConvertFieldDescriptor(StateResponse.values())
                        ),
                        customResponseFields("custom-response", beneathPath("data.volunteeringCategory").withSubsectionId("volunteeringCategory"),
                                attributes(key("title").value("봉사 유형 카테고리")),
                                enumConvertFieldDescriptor(VolunteeringCategory.values())
                        ),
                        customResponseFields("custom-response", beneathPath("data.volunteerType").withSubsectionId("volunteerType"),
                                attributes(key("title").value("봉사자 유형")),
                                enumConvertFieldDescriptor(VolunteerType.values())
                        ),
                        customResponseFields("custom-response", beneathPath("data.volunteeringType").withSubsectionId("volunteeringType"),
                                attributes(key("title").value("봉사 타입")),
                                enumConvertFieldDescriptor(VolunteeringType.values())
                        ),
                        customResponseFields("custom-response", beneathPath("data.period").withSubsectionId("period"),
                                attributes(key("title").value("반복 주기")),
                                enumConvertFieldDescriptor(Period.values())
                        ),
                        customResponseFields("custom-response", beneathPath("data.week").withSubsectionId("week"),
                                attributes(key("title").value("반복 주")),
                                enumConvertFieldDescriptor(Week.values())
                        ),
                        customResponseFields("custom-response", beneathPath("data.day").withSubsectionId("day"),
                                attributes(key("title").value("반복 요일")),
                                enumConvertFieldDescriptor(Day.values())
                        )
                ));
    }

    private FieldDescriptor[] enumConvertFieldDescriptor(CodeCommonType[] enumTypes) {
        return Arrays.stream(enumTypes)
                .map(enumType -> fieldWithPath(enumType.getId()).description(enumType.getDesc()))
                .toArray(FieldDescriptor[]::new);
    }
    public static CustomResponseFieldsSnippet customResponseFields(String type,
                                                                   PayloadSubsectionExtractor<?> subsectionExtractor,
                                                                   Map<String,Object> attribute, FieldDescriptor... descriptors){
        return new CustomResponseFieldsSnippet(type, subsectionExtractor, Arrays.asList(descriptors), attribute, true);
    }
}
