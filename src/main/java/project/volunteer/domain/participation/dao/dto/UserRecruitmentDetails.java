package project.volunteer.domain.participation.dao.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;

import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor
public class UserRecruitmentDetails {
    private Long no;
    private String imagePath;
    private LocalDate startDay;
    private LocalDate endDay;
    private String title;
    private String sido;
    private String sigungu;
    private String details;
    private VolunteeringCategory volunteeringCategory;
    private VolunteeringType volunteeringType;
    private Boolean isIssued;
    private VolunteerType volunteerType;

    public UserRecruitmentDetails(Long no, String imagePath, LocalDate startDay, LocalDate endDay,
                                  String title, String sido, String sigungu, String details,
                                  VolunteeringCategory volunteeringCategory, VolunteeringType volunteeringType,
                                  Boolean isIssued, VolunteerType volunteerType) {
        this.no = no;
        this.imagePath = imagePath;
        this.startDay = startDay;
        this.endDay = endDay;
        this.title = title;
        this.sido = sido;
        this.sigungu = sigungu;
        this.details = details;
        this.volunteeringCategory = volunteeringCategory;
        this.volunteeringType = volunteeringType;
        this.isIssued = isIssued;
        this.volunteerType = volunteerType;
    }
}
