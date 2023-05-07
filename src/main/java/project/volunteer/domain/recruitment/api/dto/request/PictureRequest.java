package project.volunteer.domain.recruitment.api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PictureRequest {

    @NotNull
    @Range(min = 0, max = 1)
    private Integer type; //0(static),1(upload)

    @NotNull
    @Length(min = 1, max = 10)
    private String staticImage;

    private MultipartFile uploadImage;

}
