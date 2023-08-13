package project.volunteer.domain.recruitment.api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PictureRequest {

    @NotNull
    private Boolean isStaticImage;

    private MultipartFile uploadImage;
}
