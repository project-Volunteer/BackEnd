package project.volunteer.restdocs.document;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.PayloadSubsectionExtractor;
import org.springframework.test.web.servlet.MockMvc;
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
