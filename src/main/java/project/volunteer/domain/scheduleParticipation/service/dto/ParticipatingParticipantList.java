package project.volunteer.domain.scheduleParticipation.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.volunteer.domain.scheduleParticipation.repository.dto.ScheduleParticipationDetail;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ParticipatingParticipantList {
    private String nickname;
    private String email;
    private String profile;

    public static ParticipatingParticipantList from(ScheduleParticipationDetail detail) {
        return new ParticipatingParticipantList(detail.getNickname(), detail.getEmail(), detail.getProfile());
    }

}
