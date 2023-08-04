package project.volunteer.domain.image.application.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;
import project.volunteer.domain.image.domain.ImageType;
import project.volunteer.global.common.component.RealWorkCode;
import project.volunteer.domain.recruitment.api.dto.request.PictureRequest;

@Getter
@Setter
@NoArgsConstructor
public class ImageParam {

    private RealWorkCode code; //log, recruitment, user...
    private Long no; //log, recruitment, user... 고유 번호
    private ImageType imageType; //static or upload image
    private String staticImageCode; //static
    private MultipartFile uploadImage; //upload

    @Builder
    public ImageParam(RealWorkCode code, Long no, ImageType imageType, String staticImageCode, MultipartFile uploadImage) {
        this.code = code;
        this.no = no;
        this.imageType = imageType;
        this.staticImageCode = staticImageCode;
        this.uploadImage = uploadImage;
    }

    public ImageParam(RealWorkCode code, Long no, PictureRequest form) {
        this.code = code;
        this.no = no;
        this.imageType = ImageType.of(form.getType());
        this.staticImageCode = form.getStaticImage();
        this.uploadImage = form.getUploadImage();
    }
}
