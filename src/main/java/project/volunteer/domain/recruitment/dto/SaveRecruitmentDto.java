package project.volunteer.domain.recruitment.dto;

import lombok.Getter;
import lombok.Setter;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class SaveRecruitmentDto {

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

}
