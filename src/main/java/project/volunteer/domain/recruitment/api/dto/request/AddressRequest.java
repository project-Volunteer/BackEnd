package project.volunteer.domain.recruitment.api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AddressRequest {

    @NotNull
    @Length(min = 1, max = 5)
    private String sido; //시,구

    @NotNull
    @Length(min=1, max = 10)
    private String sigungu; //시,군,구

    @NotNull
    @Length(min = 1, max = 50)
    private String details;

    @NotNull
    @Length(min = 1, max = 255)
    private String fullName;

    @NotNull
    private Float latitude;

    @NotNull
    private Float longitude;
}
