package project.volunteer.domain.scheduleParticipation.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CompleteApproval {

    @NotNull
    private List<Long> completedList;
}
