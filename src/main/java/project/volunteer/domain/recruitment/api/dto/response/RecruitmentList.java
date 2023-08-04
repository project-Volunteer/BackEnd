package project.volunteer.domain.recruitment.api.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.volunteer.domain.recruitment.dao.queryDto.dto.RecruitmentListQuery;
import project.volunteer.domain.recruitment.dto.PictureDetails;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class RecruitmentList {
    private Long no;
    private String volunteeringCategory;
    private PictureDetails picture;
    private String title;
    private String sido;
    private String sigungu;
    private String startDay;
    private String endDay;
    private String volunteeringType;
    private Boolean isIssued;
    private Integer volunteerNum;
    private Integer currentVolunteerNum;
    private String volunteerType;

    //TODO: 정적 팩터리 메서드 리팩토링
    public RecruitmentList(RecruitmentListQuery recruitmentDto) {
        this.no = recruitmentDto.getNo();
        this.volunteeringCategory = recruitmentDto.getCategory().getId();
        this.picture = new PictureDetails(recruitmentDto.getStaticImage(), recruitmentDto.getUploadImage());
        this.title = recruitmentDto.getTitle();
        this.sido = recruitmentDto.getSido();
        this.sigungu = recruitmentDto.getSigungu();
        this.startDay = recruitmentDto.getStartDay().format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
        this.endDay = recruitmentDto.getEndDay().format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
        this.volunteeringType = recruitmentDto.getVolunteeringType().getId();
        this.volunteerNum = recruitmentDto.getVolunteerNum();
        this.isIssued = recruitmentDto.getIsIssued();
        //TODO: 쿼리에서 Integer로 조회가 안되서?
        this.currentVolunteerNum = recruitmentDto.getCurrentVolunteerNum().intValue(); //Long 이지만 int 를 실제로 넘을수 없다.(모집 인원수가 int로 정해져있어서)
        this.volunteerType = recruitmentDto.getVolunteerType().getId();
    }

}
