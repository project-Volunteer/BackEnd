package project.volunteer.domain.recruitment.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.volunteer.domain.recruitment.repository.queryDto.dto.RecruitmentListQuery;
import project.volunteer.domain.recruitment.application.dto.query.detail.PictureDetail;

import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
public class RecruitmentList {
    private Long no;
    private String volunteeringCategory;
    private PictureDetail picture;
    private String title;
    private String sido;
    private String sigungu;
    private String fullName;
    private String startDay;
    private String endDay;
    private String volunteeringType;
    private Boolean isIssued;
    private Integer volunteerNum;
    private Integer currentVolunteerNum;
    private String volunteerType;

    //TODO: 정적 팩터리 메서드 리팩토링
    public static RecruitmentList createRecruitmentList(RecruitmentListQuery recruitmentDto, Long currentVolunteerNum) {
        RecruitmentList dto = new RecruitmentList();
        dto.no = recruitmentDto.getNo();
        dto.volunteeringCategory = recruitmentDto.getCategory().getId();
        dto.title = recruitmentDto.getTitle();
        dto.sido = recruitmentDto.getSido();
        dto.sigungu = recruitmentDto.getSigungu();
        dto.fullName = recruitmentDto.getFullName();
        dto.startDay = recruitmentDto.getStartDay().format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
        dto.endDay = recruitmentDto.getEndDay().format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
        dto.volunteeringType = recruitmentDto.getVolunteeringType().getId();
        dto.volunteerNum = recruitmentDto.getMaxParticipationNum();
        dto.isIssued = recruitmentDto.getIsIssued();
        //TODO: 쿼리에서 Integer로 조회가 안되서?
        dto.currentVolunteerNum = currentVolunteerNum.intValue(); //Long 이지만 int 를 실제로 넘을수 없다.(모집 인원수가 int로 정해져있어서)
        dto.volunteerType = recruitmentDto.getVolunteerType().getId();
        return dto;
    }

}
