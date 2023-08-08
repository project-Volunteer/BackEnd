package project.volunteer.domain.recruitment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.util.StringUtils;
import project.volunteer.domain.image.domain.ImageType;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class PictureDetails {

    private String type;
    private String staticImage;
    private String uploadImage;

    public PictureDetails(String staticImage, String uploadImage){

        if(StringUtils.hasText(staticImage))
            this.type = ImageType.STATIC.getId();
        else if(StringUtils.hasText(uploadImage))
            this.type = ImageType.UPLOAD.getId();

        this.staticImage = staticImage;
        this.uploadImage = uploadImage;
    }

}
