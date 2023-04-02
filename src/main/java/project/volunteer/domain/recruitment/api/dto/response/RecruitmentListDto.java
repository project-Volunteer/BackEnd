package project.volunteer.domain.recruitment.api.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.volunteer.domain.recruitment.dao.queryDto.dto.RecruitmentQueryDto;
import project.volunteer.domain.recruitment.dto.PictureDto;
import project.volunteer.domain.recruitment.dto.RepeatPeriodDto;

import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
public class RecruitmentListDto {

    private Long no;
    private PictureDto picture;
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
    private RepeatPeriodDto repeatPeriodDto;
    private String volunteerType;

    public RecruitmentListDto(RecruitmentQueryDto recruitmentDto) {
        this.no = recruitmentDto.getNo();
        this.picture = new PictureDto(recruitmentDto.getStaticImage(), recruitmentDto.getUploadImage());
        this.title = recruitmentDto.getTitle();
        this.sido = recruitmentDto.getSido();
        this.sigungu = recruitmentDto.getSigungu();
        this.startDay = recruitmentDto.getStartDay().format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
        this.endDay = recruitmentDto.getEndDay().format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
        this.volunteeringType = recruitmentDto.getVolunteeringType().getViewName();
        this.volunteerNum = recruitmentDto.getVolunteerNum();
        this.currentVolunteerNum = recruitmentDto.getCurrentVolunteerNum().intValue(); //Long 이지만 int 를 실제로 넘을수 없다.(모집 인원수가 int로 정해져있어서)
        this.progressTime = recruitmentDto.getProgressTime();
        this.repeatPeriodDto = new RepeatPeriodDto(recruitmentDto.getRepeatPeriodList());
        this.volunteerType = recruitmentDto.getVolunteerType().getViewName();
    }

}
