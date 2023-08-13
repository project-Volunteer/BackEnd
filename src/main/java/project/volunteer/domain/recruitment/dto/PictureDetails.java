package project.volunteer.domain.recruitment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.util.StringUtils;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class PictureDetails {

    private Boolean isStaticImage;
    private String uploadImage;

    public PictureDetails(Boolean isStaticImage, String uploadImage){
        this.isStaticImage = isStaticImage;
        this.uploadImage = uploadImage;
    }
}
