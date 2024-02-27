package project.volunteer.domain.recruitment.application.dto.query.detail;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ParticipantDetail {
    private Long recruitmentParticipationNo;
    private String nickName;
    private String imageUrl;

}
