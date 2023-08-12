package project.volunteer.domain.user.api.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.volunteer.domain.recruitment.dto.PictureDetails;

@Getter
@Setter
@NoArgsConstructor
public class JoinRecruitmentList {
    private Long no;
    private PictureDetails picture;
    private String startDay;
    private String endDay;
    private String title;
    private String sido;
    private String sigungu;
    private String details;
    private String volunteeringCategory;
    private String volunteeringType;
    private Boolean isIssued;
    private String volunteerType;

    public JoinRecruitmentList(Long no, String startDay, String endDay, String title,
                               String sido, String sigungu, String details, String volunteeringCategory,
                               String volunteeringType, Boolean isIssued, String volunteerType) {
        this.no = no;
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
