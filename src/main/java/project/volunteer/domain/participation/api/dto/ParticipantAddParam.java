package project.volunteer.domain.participation.api.dto;

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
