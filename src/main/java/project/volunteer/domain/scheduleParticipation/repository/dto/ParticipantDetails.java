package project.volunteer.domain.scheduleParticipation.repository.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.volunteer.global.common.component.ParticipantState;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ParticipantDetails {
    Long userNo;
    String nickname;
    String email;
    String profile;
    ParticipantState state;

    public Boolean isEqualParticipantState(ParticipantState state){
        return this.state.equals(state);
    }
}
