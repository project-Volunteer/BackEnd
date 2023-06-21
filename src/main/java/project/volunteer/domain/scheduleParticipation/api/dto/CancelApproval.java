package project.volunteer.domain.scheduleParticipation.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CancelApproval {

    @NotNull
    private Long no;
}
