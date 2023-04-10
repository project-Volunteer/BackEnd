package project.volunteer.domain.recruitment.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.volunteer.domain.recruitment.api.dto.request.SaveRecruitForm;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.global.common.component.Address;
import project.volunteer.global.common.component.Coordinate;
import project.volunteer.global.common.component.Timetable;
import project.volunteer.global.util.LegacyCodeEnumValueConverterUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
public class SaveRecruitDto {

    private String title;
    private String content;
    private VolunteeringCategory volunteeringCategory;
    private VolunteeringType volunteeringType;
    private VolunteerType volunteerType;
    private Integer volunteerNum;
    private Boolean isIssued;
    private String organizationName;
    private Address address;
    private Coordinate coordinate;
    private Timetable timetable;
    private Boolean isPublished;

    public SaveRecruitDto(SaveRecruitForm form) {

        this.volunteeringCategory = LegacyCodeEnumValueConverterUtils.ofLegacyCode(VolunteeringCategory.class, form.getVolunteeringCategory());
        this.organizationName = form.getOrganizationName();
        this.address = Address.builder()
                .sido(form.getAddress().getSido())
                .sigungu(form.getAddress().getSigungu())
                .details(form.getAddress().getDetails())
                .build();
        this.coordinate = Coordinate.builder()
                .latitude(form.getAddress().getLatitude())
                .longitude(form.getAddress().getLongitude())
                .build();
        this.isIssued = form.getIsIssued();
        this.volunteerType = LegacyCodeEnumValueConverterUtils.ofLegacyCode(VolunteerType.class, form.getVolunteerType());
        this.volunteerNum = form.getVolunteerNum();
        this.volunteeringType = VolunteeringType.of(form.getVolunteeringType());
        this.timetable = new Timetable(
                LocalDate.parse(form.getStartDay(), DateTimeFormatter.ofPattern("MM-dd-yyyy")),
                LocalDate.parse(form.getEndDay(), DateTimeFormatter.ofPattern("MM-dd-yyyy")),
                LocalTime.parse(form.getStartTime(), DateTimeFormatter.ofPattern("HH:mm:ss")),
                form.getProgressTime()
                );
        this.title = form.getTitle();
        this.content = form.getContent();
        this.isPublished = form.getIsPublished();
    }

    public SaveRecruitDto(String volunteeringCategory, String organizationName, String sido, String sigungu, String details, Float latitude, Float longitude,
                          Boolean isIssued, String volunteerType, Integer volunteerNum, String volunteeringType,
                          String startDay, String endDay, String startTime, Integer progressTime, String title, String content, Boolean isPublished) {

        this.volunteeringCategory = LegacyCodeEnumValueConverterUtils.ofLegacyCode(VolunteeringCategory.class, volunteeringCategory);
        this.organizationName = organizationName;
        this.address = Address.builder()
                .sido(sido).sigungu(sigungu).details(details).build();
        this.coordinate = Coordinate.builder()
                .latitude(latitude).longitude(longitude).build();
        this.isIssued = isIssued;
        this.volunteerType = LegacyCodeEnumValueConverterUtils.ofLegacyCode(VolunteerType.class, volunteerType);
        this.volunteerNum = volunteerNum;
        this.volunteeringType = VolunteeringType.of(volunteeringType);
        this.timetable = new Timetable(
                LocalDate.parse(startDay, DateTimeFormatter.ofPattern("MM-dd-yyyy")),
                LocalDate.parse(endDay, DateTimeFormatter.ofPattern("MM-dd-yyyy")),
                LocalTime.parse(startTime, DateTimeFormatter.ofPattern("HH:mm:ss")),
                progressTime
        );
        this.title = title;
        this.content = content;
        this.isPublished = isPublished;
    }

}
