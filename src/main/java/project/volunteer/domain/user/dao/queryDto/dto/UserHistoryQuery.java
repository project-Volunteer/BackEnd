package project.volunteer.domain.user.dao.queryDto.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class UserHistoryQuery {
    private Long no;
    private String staticImage;
    private String uploadImage;
    private LocalDate endDay;
    private String title;
    private String sido;
    private String sigungu;
    private VolunteeringCategory volunteeringCategory;
    private VolunteeringType volunteeringType;
    private Boolean isIssued;
    private VolunteerType volunteerType;
    private Integer progressTime;


    @QueryProjection
    public UserHistoryQuery(Long no, String staticImage, String uploadImage, LocalDate endDay, String title
                            , String sido, String sigungu, VolunteeringCategory volunteeringCategory, VolunteeringType volunteeringType
                            , Boolean isIssued, VolunteerType volunteerType, Integer progressTime) {
        this.no = no;
        this.staticImage = staticImage;
        this.uploadImage = uploadImage;
        this.endDay = endDay;
        this.title = title;
        this.sido = sido;
        this.sigungu = sigungu;
        this.volunteeringCategory = volunteeringCategory;
        this.volunteeringType = volunteeringType;
        this.isIssued = isIssued;
        this.volunteerType = volunteerType;
        this.progressTime = progressTime;
    }
}
