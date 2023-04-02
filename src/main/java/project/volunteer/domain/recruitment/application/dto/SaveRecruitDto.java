package project.volunteer.domain.recruitment.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.volunteer.domain.recruitment.api.dto.request.SaveRecruitForm;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.global.common.component.Timetable;

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
    private String sido;
    private String sigungu;
    private String details;
    private Float latitude;
    private Float longitude;
    private Timetable timetable;
    private Boolean isPublished;

    public SaveRecruitDto(SaveRecruitForm form) {

        this.volunteeringCategory = VolunteeringCategory.ofCode(form.getVolunteeringCategory());
        this.organizationName = form.getOrganizationName();
        this.sido = form.getAddress().getSido();
        this.sigungu = form.getAddress().getSigungu();
        this.details = form.getAddress().getDetails();
        this.latitude = form.getAddress().getLatitude();
        this.longitude = form.getAddress().getLongitude();
        this.isIssued = form.getIsIssued();
        this.volunteerType = VolunteerType.of(form.getVolunteerType());
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

        this.volunteeringCategory = VolunteeringCategory.ofCode(volunteeringCategory);
        this.organizationName = organizationName;
        this.sido = sido;
        this.sigungu = sigungu;
        this.details = details;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isIssued = isIssued;
        this.volunteerType = VolunteerType.of(volunteerType);
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
