package project.volunteer.domain.recruitmentParticipation.api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ParticipantAddParam {

    @NotEmpty
    private List<Long> recruitmentParticipationNos;

}
