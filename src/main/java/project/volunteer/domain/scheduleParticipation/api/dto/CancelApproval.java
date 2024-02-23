package project.volunteer.domain.scheduleParticipation.api.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CancelApproval {

    @NotNull
    private List<Long> no;
}
