package project.volunteer.domain.recruitment.dao.queryDto.dto;

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
public class RecruitmentListQuery {

    private Long no;
    private VolunteeringCategory category;
    private String title;
    private String sido;
    private String sigungu;
    private LocalDate startDay;
    private LocalDate endDay;
    private VolunteeringType volunteeringType;
    private VolunteerType volunteerType;
    private Boolean isIssued;
    private Integer volunteerNum;
    private Long currentVolunteerNum; //참여자 매핑 테이블에서 추출
    private String uploadImage; //storage 테이블에서 추출

    @QueryProjection
    public RecruitmentListQuery(Long no, VolunteeringCategory category, String title, String sido, String sigungu, LocalDate startDay, LocalDate endDay, VolunteeringType volunteeringType,
                                VolunteerType volunteerType, Boolean isIssued, int volunteerNum, String uploadImage){
        this.no  = no;
        this.category = category;
        this.title = title;
        this.sido = sido;
        this.sigungu = sigungu;
        this.startDay = startDay;
        this.endDay = endDay;
        this.volunteeringType = volunteeringType;
        this.volunteerType = volunteerType;
        this.isIssued = isIssued;
        this.volunteerNum = volunteerNum;
        this.uploadImage = uploadImage;
    }
}
