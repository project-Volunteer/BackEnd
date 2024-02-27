package project.volunteer.domain.user.dao.queryDto.dto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.querydsl.core.annotations.QueryProjection;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.recruitment.application.dto.query.detail.PictureDetail;

@Getter
@Setter
@NoArgsConstructor
public class
UserRecruitmentJoinRequestQuery {
	private Long no;
	private PictureDetail picture;
	private String startDay;
	private String endDay;
	private String title;
	private String sido;
	private String sigungu;
	private VolunteeringCategory volunteeringCategory;
	private VolunteeringType volunteeringType;
	private Boolean isIssued;
	private VolunteerType volunteerType;
	
	@QueryProjection
	public UserRecruitmentJoinRequestQuery(Long no, String uploadImage, LocalDate startDay,
										LocalDate endDay, String title, String sido, String sigungu, 
										VolunteeringCategory volunteeringCategory, VolunteeringType volunteeringType, 
										Boolean isIssued, VolunteerType volunteerType) {
		this.no = no;
		if(uploadImage == null){
			picture = new PictureDetail(true, null);
		}else{
			picture = new PictureDetail(false, uploadImage);
		}
		this.startDay = startDay.format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
		this.endDay = endDay.format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
		this.title = title;
		this.sido = sido;
		this.sigungu = sigungu;
		this.volunteeringCategory = volunteeringCategory;
		this.volunteeringType = volunteeringType;
		this.isIssued = isIssued;
		this.volunteerType = volunteerType;
	}
	
}
