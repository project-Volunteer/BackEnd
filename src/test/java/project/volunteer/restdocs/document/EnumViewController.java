package project.volunteer.restdocs.document;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import project.volunteer.global.common.component.HourFormat;
import project.volunteer.global.common.converter.CodeCommonType;
import project.volunteer.global.common.dto.StateResponse;
import project.volunteer.restdocs.document.dto.APIResponseDto;
import project.volunteer.restdocs.document.dto.EnumDocs;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class EnumViewController {

    @GetMapping("/docs/enums")
    public APIResponseDto<EnumDocs> enums(){

        Map<String,String > hourFormat = getDocs(HourFormat.values());
        Map<String,String> clientState = getDocs(StateResponse.values());

        return APIResponseDto.of(EnumDocs.builder()
                .hourFormat(hourFormat)
                .clientState(clientState)
                .build());
    }
    private Map<String, String> getDocs(CodeCommonType[] commonTypes){
        return Arrays.stream(commonTypes)
                .collect(Collectors.toMap(CodeCommonType::getId, CodeCommonType::getDesc));
    }
}
