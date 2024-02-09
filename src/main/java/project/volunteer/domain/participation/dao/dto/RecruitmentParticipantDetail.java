package project.volunteer.domain.participation.dao.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.volunteer.global.common.component.ParticipantState;

@Getter
@Setter
@NoArgsConstructor
public class RecruitmentParticipantDetail {
    private ParticipantState state;
    private Long userNo;
    private String nickName;
    private String imageUrl;

    public RecruitmentParticipantDetail(ParticipantState state, Long userNo, String nickName, String imageUrl){
        this.state = state;
        this.userNo = userNo;
        this.nickName = nickName;
        this.imageUrl = imageUrl;
    }
}
