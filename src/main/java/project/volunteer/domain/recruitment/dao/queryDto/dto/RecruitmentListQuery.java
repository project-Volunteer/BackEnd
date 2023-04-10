package project.volunteer.domain.recruitment.dao.queryDto.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.repeatPeriod.domain.Day;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class RecruitmentListQuery {

    private Long no;
    private String title;
    private String sido;
    private String sigungu;
    private LocalDate startDay;
    private LocalDate endDay;
    private VolunteeringType volunteeringType;
    private VolunteerType volunteerType;
    private Boolean isissued;
    private Integer volunteerNum;
    private int progressTime;

    private Long currentVolunteerNum; //참여자 매핑 테이블에서 추출
    private List<Day> days = new ArrayList<>(); //반복 주기 테이블에서 추출
    private String staticImage; //image 테이블에서 추출
    private String uploadImage; //storage 테이블에서 추출

    @QueryProjection
    public RecruitmentListQuery(Long no, String title, String sido, String sigungu, LocalDate startDay, LocalDate endDay, VolunteeringType volunteeringType,
                                VolunteerType volunteerType, Boolean isIssued, int volunteerNum, int progressTime,
                                String staticImage, String uploadImage){
        this.no  = no;
        this.title = title;
        this.sido = sido;
        this.sigungu = sigungu;
        this.startDay = startDay;
        this.endDay = endDay;
        this.volunteeringType = volunteeringType;
        this.volunteerType = volunteerType;
        this.isissued = isIssued;
        this.volunteerNum = volunteerNum;
        this.progressTime = progressTime;
        this.staticImage = staticImage;
        this.uploadImage = uploadImage;
    }
}
