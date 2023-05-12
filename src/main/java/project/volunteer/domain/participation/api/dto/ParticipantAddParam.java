package project.volunteer.domain.participation.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ParticipantAddParam {

    @NotNull
    private Long recruitmentNo;
    @NotEmpty
    private List<Long> userNos;

}
