package project.volunteer.domain.scheduleParticipation.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CancelledParticipantList {
    Long no;
    String nickname;
    String email;
    String profile;
}
