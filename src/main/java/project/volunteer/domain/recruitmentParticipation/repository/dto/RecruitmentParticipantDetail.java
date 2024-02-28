package project.volunteer.domain.recruitmentParticipation.repository.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import project.volunteer.global.common.component.ParticipantState;

@Getter
@Setter
@NoArgsConstructor
public class RecruitmentParticipantDetail {
    private ParticipantState state;
    private Long recruitmentParticipationNo;
    private String nickName;
    private String imageUrl;

    public RecruitmentParticipantDetail(ParticipantState state, Long recruitmentParticipationNo, String nickName,
                                        String imageUrl) {
        this.state = state;
        this.recruitmentParticipationNo = recruitmentParticipationNo;
        this.nickName = nickName;
        this.imageUrl = imageUrl;
    }
}
