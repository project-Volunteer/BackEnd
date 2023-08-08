package project.volunteer.domain.image.application;

import project.volunteer.domain.image.application.dto.ImageParam;
import project.volunteer.global.common.component.RealWorkCode;

public interface ImageService {

    public Long addImage(ImageParam saveImageDto);

    public void deleteImage(RealWorkCode code, Long no);

    public void deleteImageList(RealWorkCode code, Long no);
}
