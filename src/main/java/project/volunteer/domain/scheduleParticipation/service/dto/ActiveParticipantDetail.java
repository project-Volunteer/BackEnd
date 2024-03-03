package project.volunteer.domain.scheduleParticipation.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.volunteer.domain.scheduleParticipation.repository.dto.ScheduleParticipationDetail;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ActiveParticipantDetail {
    private String nickname;
    private String email;
    private String profile;

    public static ActiveParticipantDetail from(ScheduleParticipationDetail detail) {
        return new ActiveParticipantDetail(detail.getNickname(), detail.getEmail(), detail.getProfile());
    }

}
