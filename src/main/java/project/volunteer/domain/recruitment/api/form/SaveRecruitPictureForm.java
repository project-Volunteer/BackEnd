package project.volunteer.domain.recruitment.api.form;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SaveRecruitPictureForm {
    private Integer type; //0(static),1(upload)
    private String staticImage;
    private MultipartFile uploadImage;
}
