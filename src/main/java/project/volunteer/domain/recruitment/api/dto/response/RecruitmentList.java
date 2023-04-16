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
    private Integer progressTime;
    private List<String> repeatDay;
    private String volunteerType;

    public RecruitmentList(RecruitmentListQuery recruitmentDto) {
        this.no = recruitmentDto.getNo();
        this.picture = new PictureDetails(recruitmentDto.getStaticImage(), recruitmentDto.getUploadImage());
        this.title = recruitmentDto.getTitle();
        this.sido = recruitmentDto.getSido();
        this.sigungu = recruitmentDto.getSigungu();
        this.startDay = recruitmentDto.getStartDay().format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
        this.endDay = recruitmentDto.getEndDay().format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
        this.volunteeringType = recruitmentDto.getVolunteeringType().getViewName();
        this.volunteerNum = recruitmentDto.getVolunteerNum();
        this.currentVolunteerNum = recruitmentDto.getCurrentVolunteerNum().intValue(); //Long 이지만 int 를 실제로 넘을수 없다.(모집 인원수가 int로 정해져있어서)
        this.progressTime = recruitmentDto.getProgressTime();
        this.repeatDay = recruitmentDto.getDays().stream().map(d -> d.getViewName()).collect(Collectors.toList());
        this.volunteerType = recruitmentDto.getVolunteerType().getDesc();
    }

}
