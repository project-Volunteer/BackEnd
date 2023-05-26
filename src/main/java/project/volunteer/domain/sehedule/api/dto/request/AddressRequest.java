package project.volunteer.domain.sehedule.api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AddressRequest {

    @NotNull
    @Length(min = 1, max = 5)
    private String sido;

    @NotNull
    @Length(min=1, max = 10)
    private String sigungu;

    @NotNull
    @Length(min = 1, max = 50)
    private String details;
}
