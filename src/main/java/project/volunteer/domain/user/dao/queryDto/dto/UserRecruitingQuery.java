package project.volunteer.domain.user.dao.queryDto.dto;

import java.time.LocalDate;

import com.querydsl.core.annotations.QueryProjection;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.user.domain.User;

@Getter
@Setter
@NoArgsConstructor
public class UserRecruitingQuery {
	private Long no;
	private String uploadImage;
	private LocalDate startDay;
	private LocalDate endDay;
	private String title;
	private String sido;
	private String sigungu;
	private VolunteeringCategory volunteeringCategory;
	private VolunteeringType volunteeringType;
	private Boolean isIssued;
	private VolunteerType volunteerType;
	private int volunteerNum;
	private User writer;
	private long currentVolunteerNum;

	@QueryProjection
	public UserRecruitingQuery(Long no,String uploadImage, LocalDate startDay, LocalDate endDay,
								String title, String sido, String sigungu, VolunteeringCategory volunteeringCategory,
								VolunteeringType volunteeringType, Boolean isIssued, VolunteerType volunteerType, 
								int volunteerNum, User writer, long currentVolunteerNum) {
		this.no = no;
		this.uploadImage = uploadImage;
		this.startDay = startDay;
		this.endDay = endDay;
		this.title = title;
		this.sido = sido;
		this.sigungu = sigungu;
		this.volunteeringCategory = volunteeringCategory;
		this.volunteeringType = volunteeringType;
		this.isIssued = isIssued;
		this.volunteerType = volunteerType;
		this.volunteerNum = volunteerNum;
		this.writer = writer;
		this.currentVolunteerNum = currentVolunteerNum;
	}
}
