package project.volunteer.domain.image.application.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;
import project.volunteer.domain.image.domain.ImageType;
import project.volunteer.domain.image.domain.RealWorkCode;
import project.volunteer.domain.recruitment.api.dto.request.SaveRecruitPictureForm;

@Getter
@Setter
@NoArgsConstructor
public class SaveImageDto {

    private RealWorkCode code; //log, recruitment, user...
    private Long no; //log, recruitment, user... 고유 번호
    private ImageType imageType; //static or upload image
    private String staticImageCode; //static
    private MultipartFile uploadImage; //upload

    @Builder
    public SaveImageDto(RealWorkCode code, Long no, ImageType imageType, String staticImageCode, MultipartFile uploadImage) {
        this.code = code;
        this.no = no;
        this.imageType = imageType;
        this.staticImageCode = staticImageCode;
        this.uploadImage = uploadImage;
    }

    public SaveImageDto(RealWorkCode code, Long no, SaveRecruitPictureForm form) {
        this.code = code;
        this.no = no;
        this.imageType = ImageType.of(form.getType());
        //업로드 이미지일 경우 "staticImageCode" 컬럼은 null
        this.staticImageCode = (this.imageType.name().equals(ImageType.UPLOAD.name()))
                ?null: form.getStaticImage();
        this.uploadImage = form.getUploadImage();
    }
}
