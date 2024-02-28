package project.volunteer.domain.scheduleParticipation.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.volunteer.domain.scheduleParticipation.repository.dto.ScheduleParticipationDetail;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CancelledParticipantDetail {
    private Long scheduleParticipationNo;
    private String nickname;
    private String email;
    private String profile;

    public static CancelledParticipantDetail from(ScheduleParticipationDetail scheduleParticipationDetail) {
        return new CancelledParticipantDetail(scheduleParticipationDetail.getScheduleParticipationNo(),
                scheduleParticipationDetail.getNickname(), scheduleParticipationDetail.getEmail(),
                scheduleParticipationDetail.getProfile());
    }
}
