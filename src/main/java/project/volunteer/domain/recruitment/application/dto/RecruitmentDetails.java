package project.volunteer.domain.recruitment.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.dto.PictureDetails;
import project.volunteer.domain.recruitment.dto.RepeatPeriodDetails;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class RecruitmentDetails {

    private Long no;
    private String volunteeringCategory;
    private String organizationName;
    private Boolean isIssued;
    private String volunteeringType;
    private String volunteerType;
    private Integer volunteerNum;
    private String startDay;
    private String endDay;
    private String hourFormat;
    private String startTime;
    private Integer progressTime;
    private String title;
    private String content;
    private AddressDetails address;
    private PictureDetails picture;
    private RepeatPeriodDetails repeatPeriod;
    private WriterDetails author;

    public RecruitmentDetails(Recruitment recruitment){
        this.no = recruitment.getRecruitmentNo();
        this.volunteeringCategory = recruitment.getVolunteeringCategory().getId();
        this.organizationName = recruitment.getOrganizationName();
        this.address = new AddressDetails(recruitment.getAddress(), recruitment.getCoordinate());
        this.isIssued = recruitment.getIsIssued();
        this.volunteeringType = recruitment.getVolunteeringType().getId();
        this.volunteerType = recruitment.getVolunteerType().getId();
        this.volunteerNum = recruitment.getVolunteerNum();
        this.startDay = recruitment.getVolunteeringTimeTable().getStartDay().format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
        this.endDay = recruitment.getVolunteeringTimeTable().getEndDay().format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
        this.hourFormat = recruitment.getVolunteeringTimeTable().getHourFormat().getId();
        this.startTime = recruitment.getVolunteeringTimeTable().getStartTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        this.progressTime = recruitment.getVolunteeringTimeTable().getProgressTime();
        this.title = recruitment.getTitle();
        this.content = recruitment.getContent();
    }

}
