package project.volunteer.restdocs.document;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.repeatPeriod.domain.Day;
import project.volunteer.domain.repeatPeriod.domain.Period;
import project.volunteer.domain.repeatPeriod.domain.Week;
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
        Map<String,String> volunteeringCategory = getDocs(VolunteeringCategory.values());
        Map<String,String> volunteerType = getDocs(VolunteerType.values());
        Map<String,String> volunteeringType = getDocs(VolunteeringType.values());
        Map<String,String> period = getDocs(Period.values());
        Map<String,String> week = getDocs(Week.values());
        Map<String,String> day = getDocs(Day.values());

        return APIResponseDto.of(EnumDocs.builder()
                .hourFormat(hourFormat)
                .clientState(clientState)
                .volunteeringCategory(volunteeringCategory)
                .volunteerType(volunteerType)
                .volunteeringType(volunteeringType)
                .period(period)
                .week(week)
                .day(day)
                .build());
    }
    private Map<String, String> getDocs(CodeCommonType[] commonTypes){
        return Arrays.stream(commonTypes)
                .collect(Collectors.toMap(CodeCommonType::getId, CodeCommonType::getDesc));
    }
}
