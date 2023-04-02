package project.volunteer.domain.recruitment.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.dto.PictureDto;
import project.volunteer.domain.recruitment.dto.RepeatPeriodDto;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class RecruitmentDto {

    private Long no;
    private String volunteeringCategory;
    private String organizationName;
    private AddressDto address;
    private Boolean isIssued;
    private String volunteeringType;
    private String volunteerType;
    private Integer volunteerNum;
    private List<ParticipantDto> currentVolunteer;
    private WriterDto author;
    private String startDay;
    private String endDay;
    private String startTime;
    private Integer progressTime;
    private RepeatPeriodDto repeatPeriod;
    private PictureDto picture;
    private String title;
    private String content;

    public RecruitmentDto(Recruitment recruitment){
        this.no = recruitment.getRecruitmentNo();
        this.volunteeringCategory = recruitment.getVolunteeringCategory().getViewName();
        this.organizationName = recruitment.getOrganizationName();
        this.address = new AddressDto(recruitment.getAddress());
        this.isIssued = recruitment.getIsIssued();
        this.volunteeringType = recruitment.getVolunteeringType().getViewName();
        this.volunteerType = recruitment.getVolunteerType().getViewName();
        this.volunteerNum = recruitment.getVolunteerNum();
        this.startDay = recruitment.getVolunteeringTimeTable().getStartDay().format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
        this.endDay = recruitment.getVolunteeringTimeTable().getEndDay().format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
        this.startTime = recruitment.getVolunteeringTimeTable().getStartTime().format(DateTimeFormatter.ofPattern("HH-mm-ss"));
        this.progressTime = recruitment.getVolunteeringTimeTable().getProgressTime();
        this.title = recruitment.getTitle();
        this.content = recruitment.getContent();
    }

}
