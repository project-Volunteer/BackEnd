package project.volunteer.domain.logboard.dao.dto;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.volunteer.domain.like.domain.Like;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;

@Setter
@Getter
@NoArgsConstructor
public class LogboardListQuery {
	private Long no;
	private Long writerNo;
	private String profile;
	private String nickname;
	private LocalDateTime createdDay;
	private VolunteeringCategory volunteeringCategory;
	private String content;
	private Integer likeCnt;
	private boolean isLikeMe;

	@QueryProjection
	public LogboardListQuery(Long no, Long writerNo, String profile, String nickname, LocalDateTime createdDay,
			VolunteeringCategory volunteeringCategory, String content, Integer likeCnt, Like isLikeMe) {
		this.no = no;
		this.writerNo = writerNo;
		this.profile = profile;
		this.nickname = nickname;
		this.createdDay = createdDay;
		this.volunteeringCategory = volunteeringCategory;
		this.content = content;
		this.likeCnt = likeCnt;
		this.isLikeMe = isLikeMe==null?false:true;
	}

	
}
