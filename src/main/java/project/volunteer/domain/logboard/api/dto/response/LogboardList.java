package project.volunteer.domain.logboard.api.dto.response;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
	private String createdDay;
	private List<String> pictures;
	private String volunteeringCategory;
	private String content;
	private Integer likeCnt;
	private boolean isLikeMe;
	private Integer commentCnt;
	

	public LogboardList(Long no, Long writerNo, String profile, String nickname, LocalDateTime createdDay,
			VolunteeringCategory volunteeringCategory, String content, Integer likeCnt, boolean isLikeMe) {
		this.no = no;
		this.writerNo = writerNo;
		this.profile = profile;
		this.nickname = nickname;
		this.createdDay = createdDay.format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
		this.volunteeringCategory = volunteeringCategory.getId();
		this.content = content;
		this.likeCnt = likeCnt;
		this.isLikeMe = isLikeMe;
	}
	
	public void setPicturesFromImageDomain(List<Image> imageList) {
		List<String> imagePath = new ArrayList<>();
		
		for(Image i : imageList) {
			imagePath.add(i.getStorage().getImagePath());
		}
		
		this.pictures = imagePath;
	}

}
