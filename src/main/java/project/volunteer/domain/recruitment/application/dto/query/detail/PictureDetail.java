package project.volunteer.domain.recruitment.application.dto.query.detail;

import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PictureDetail {
    private Boolean isStaticImage;
    private String uploadImage;

    public static PictureDetail of(String uploadImagePath) {
        if (Objects.isNull(uploadImagePath)) {
            return new PictureDetail(true, null);
        } else {
            return new PictureDetail(false, uploadImagePath);
        }
    }
}
