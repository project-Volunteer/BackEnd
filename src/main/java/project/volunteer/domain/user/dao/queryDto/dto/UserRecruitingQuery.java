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
import project.volunteer.domain.recruitment.dto.PictureDetails;

@Getter
@Setter
@NoArgsConstructor
public class UserRecruitingQuery {
	private Long no;
	private PictureDetails picture;
	private String startDay;
	private String endDay;
	private String title;
	private String sido;
	private String sigungu;
	private VolunteeringCategory volunteeringCategory;
	private VolunteeringType volunteeringType;
	private Boolean isIssued;
	private VolunteerType volunteerType;
	private int volunteerNum;
	private long currentVolunteerNum;

	@QueryProjection
	public UserRecruitingQuery(Long no,String uploadImage, LocalDate startDay, LocalDate endDay,
								String title, String sido, String sigungu, VolunteeringCategory volunteeringCategory,
								VolunteeringType volunteeringType, Boolean isIssued, VolunteerType volunteerType, 
								int volunteerNum, long currentVolunteerNum) {
		this.no = no;
		if(uploadImage == null){
			picture = new PictureDetails(true, null);
		}else{
			picture = new PictureDetails(false, uploadImage);
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
		this.volunteerNum = volunteerNum;
		this.currentVolunteerNum = currentVolunteerNum;
	}
}
