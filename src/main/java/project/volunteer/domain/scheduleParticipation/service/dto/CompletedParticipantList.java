package project.volunteer.domain.scheduleParticipation.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CompletedParticipantList {

    Long no;
    String nickname;
    String email;
    String profile;
    String status;
}
