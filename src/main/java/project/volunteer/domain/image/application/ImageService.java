package project.volunteer.domain.image.application;

import project.volunteer.domain.image.domain.RealWorkCode;
import project.volunteer.domain.image.dto.SaveImageDto;

public interface ImageService {

    public Long addImage(SaveImageDto saveImageDto);
}
