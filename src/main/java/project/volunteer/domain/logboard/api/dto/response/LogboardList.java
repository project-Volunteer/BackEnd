package project.volunteer.domain.logboard.api.dto.response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import project.volunteer.domain.image.domain.Image;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class LogboardList {
	private Long no;
	private Long writerNo;
	private String profile;
	private String nickname;
	private LocalDateTime createdDay;
	private List<String> pictures;
	private VolunteeringCategory volunteeringCategory;
	private String content;
	private Integer likeCnt;
	private boolean isLikeMe;
	private Integer commentCnt;
	

	public LogboardList(Long no, Long writerNo, String profile, String nickname, LocalDateTime createdDay,
			VolunteeringCategory volunteeringCategory, String content, Integer likeCnt, boolean isLikeMe,
			Integer commentCnt) {
		this.no = no;
		this.writerNo = writerNo;
		this.profile = profile;
		this.nickname = nickname;
		this.createdDay = createdDay;
		this.volunteeringCategory = volunteeringCategory;
		this.content = content;
		this.likeCnt = likeCnt;
		this.isLikeMe = isLikeMe;
		this.commentCnt = commentCnt;
	}
	
	public void setPicturesFromImageDomain(List<Image> imageList) {
		List<String> imagePath = new ArrayList<>();
		
		for(Image i : imageList) {
			imagePath.add(i.getStorage().getImagePath());
		}
		
		this.pictures = imagePath;
	}
    
}
