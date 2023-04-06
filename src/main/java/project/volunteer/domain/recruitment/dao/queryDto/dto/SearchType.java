package project.volunteer.domain.recruitment.dao.queryDto.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.StringUtils;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.global.util.LegacyCodeEnumValueConverterUtils;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class SearchType {

    private List<VolunteeringCategory> category;
    private String sido;
    private String sigungu;
    private VolunteeringType volunteeringType;
    private VolunteerType volunteerType;
    private Boolean isIssued;

    public SearchType(List<String> category, String sido, String sigungu, String volunteeringType, String volunteerType, Boolean isIssued) {

        this.category = category.stream()
                .map(c -> LegacyCodeEnumValueConverterUtils.ofLegacyCode(VolunteeringCategory.class, c))
                .collect(Collectors.toList());
        this.sido = sido;
        this.sigungu = sigungu;
        this.volunteeringType = (StringUtils.hasText(volunteeringType))?(VolunteeringType.of(volunteeringType)):null;
        this.volunteerType = LegacyCodeEnumValueConverterUtils.ofLegacyCode(VolunteerType.class, volunteerType);
        this.isIssued = isIssued;
    }
}
