package project.volunteer.domain.logboard.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.volunteer.domain.image.domain.Image;
import project.volunteer.domain.logboard.domain.Logboard;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.user.api.dto.response.UserInfo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class LogboardDetail {
    private Long no;
    private Long writerNo;
    private String profile;
    private String nickName;
    private String createdDay;
    private List<String> pictures;
    private String volunteeringCategory;
    private String content;

    public LogboardDetail(Logboard logboard){
        this.no = logboard.getLogboardNo();
        this.writerNo = logboard.getCreateUserNo();
        this.createdDay = logboard.getCreatedDate().format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
        this.volunteeringCategory = logboard.getSchedule().getRecruitment().getVolunteeringCategory().getId();
        this.content = logboard.getContent();
    }

    public void setPicture(List<String> pictures) {
        this.pictures = pictures;
    }

    public void setWriterInfo(UserInfo userInfo){
        this.profile = userInfo.getProfile();
        this.nickName = userInfo.getNicName();
    }

}
