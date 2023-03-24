package project.volunteer.domain.recruitment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.volunteer.domain.recruitment.api.form.SaveRecruitForm;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;

import java.text.DateFormat;
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
    private String country;
    private String details;
    private Float latitude;
    private Float longitude;
    private LocalDate startDay;
    private LocalDate endDay;
    private LocalTime startTime;
    private Integer progressTime; //(1~24시간)
    private Boolean isPublished;

    public SaveRecruitDto(SaveRecruitForm form) {

        this.volunteeringCategory = VolunteeringCategory.ofCode(form.getVolunteeringCategory());
        this.organizationName = form.getOrganizationName();
        this.country = form.getAddress().getCountry();
        this.details = form.getAddress().getDetails();
        this.latitude = form.getAddress().getLatitude();
        this.longitude = form.getAddress().getLongitude();
        this.isIssued = form.getIsIssued();
        this.volunteerType = VolunteerType.of(form.getVolunteerType());
        this.volunteerNum = form.getVolunteerNum();
        this.volunteeringType = VolunteeringType.of(form.getVolunteeringType());
        this.startDay = LocalDate.parse(form.getStartDay(), DateTimeFormatter.ofPattern("MM-dd-yyyy"));
        this.endDay = LocalDate.parse(form.getEndDay(), DateTimeFormatter.ofPattern("MM-dd-yyyy"));
        this.startTime = LocalTime.parse(form.getStartTime(), DateTimeFormatter.ofPattern("HH:mm:ss"));
        this.progressTime = form.getProgressTime();
        this.title = form.getTitle();
        this.content = form.getContent();
        this.isPublished = form.getIsPublished();
    }

    public SaveRecruitDto(String volunteeringCategory, String organizationName, String country, String details, Float latitude, Float longitude,
                          Boolean isIssued, String volunteerType, Integer volunteerNum, String volunteeringType,
                          String startDay, String endDay, String startTime, Integer progressTime, String title, String content, Boolean isPublished) {

        this.volunteeringCategory = VolunteeringCategory.ofCode(volunteeringCategory);
        this.organizationName = organizationName;
        this.country = country;
        this.details = details;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isIssued = isIssued;
        this.volunteerType = VolunteerType.of(volunteerType);
        this.volunteerNum = volunteerNum;
        this.volunteeringType = VolunteeringType.of(volunteeringType);
        this.startDay = LocalDate.parse(startDay, DateTimeFormatter.ofPattern("MM-dd-yyyy"));
        this.endDay = LocalDate.parse(endDay, DateTimeFormatter.ofPattern("MM-dd-yyyy"));
        this.startTime = LocalTime.parse(startTime, DateTimeFormatter.ofPattern("HH:mm:ss"));
        this.progressTime = progressTime;
        this.title = title;
        this.content = content;
        this.isPublished = isPublished;
    }

}
