package project.volunteer.domain.recruitmentParticipation.api.dto.request;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ParticipantRemoveParam {

    @NotNull
    private List<Long> recruitmentParticipationNos;
}
