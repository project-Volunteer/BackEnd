package project.volunteer.domain.recruitment.api.form;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SaveRecruitPictureForm {

    @Min(0)
    private Integer type; //0(static),1(upload)

    @Length(min = 1, max = 10)
    @NotNull(message = "널을 허용하지 않습니다.")
    private String staticImage;

    @NotNull(message = "널을 허용하지 않습니다.")
    private MultipartFile uploadImage;

}
