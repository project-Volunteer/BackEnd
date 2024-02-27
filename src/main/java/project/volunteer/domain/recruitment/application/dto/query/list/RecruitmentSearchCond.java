package project.volunteer.domain.recruitment.application.dto.query.list;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.global.util.LegacyCodeEnumValueConverterUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RecruitmentSearchCond {
    private List<VolunteeringCategory> category;
    private String sido;
    private String sigungu;
    private VolunteeringType volunteeringType;
    private VolunteerType volunteerType;
    private Boolean isIssued;

    public static RecruitmentSearchCond of(List<String> category, String sido, String sigungu, String volunteeringType,
                                           String volunteerType, Boolean isIssued) {
        return new RecruitmentSearchCond(
                Optional.ofNullable(category)
                        .orElseGet(Collections::emptyList)
                        .stream()
                        .map(c -> LegacyCodeEnumValueConverterUtils.ofLegacyCode(VolunteeringCategory.class, c))
                        .collect(Collectors.toList()),
                sido, sigungu,
                VolunteeringType.of(volunteeringType),
                LegacyCodeEnumValueConverterUtils.ofLegacyCode(VolunteerType.class, volunteerType),
                isIssued
        );
    }
}
