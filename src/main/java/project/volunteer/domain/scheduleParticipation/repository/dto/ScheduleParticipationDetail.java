package project.volunteer.domain.scheduleParticipation.repository.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.volunteer.global.common.component.ParticipantState;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ScheduleParticipationDetail {
    private Long scheduleParticipationNo;
    private String nickname;
    private String email;
    private String profile;
    private ParticipantState state;

    public Boolean isEqualParticipantState(ParticipantState state){
        return this.state.equals(state);
    }
}
