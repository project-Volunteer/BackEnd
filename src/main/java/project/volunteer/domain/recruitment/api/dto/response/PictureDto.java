package project.volunteer.domain.recruitment.api.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.StringUtils;
import project.volunteer.domain.image.domain.Image;
import project.volunteer.domain.image.domain.ImageType;

@Getter
@Setter
@NoArgsConstructor
public class PictureDto {

    private Integer type;
    private String staticImage;
    private String uploadImage;

    public PictureDto(String staticImage, String uploadImage){

        if(!StringUtils.hasText(staticImage))
            this.type = ImageType.STATIC.getValue();
        else if(!StringUtils.hasText(uploadImage))
            this.type = ImageType.UPLOAD.getValue();

        this.staticImage = staticImage;
        this.uploadImage = uploadImage;
    }

}
