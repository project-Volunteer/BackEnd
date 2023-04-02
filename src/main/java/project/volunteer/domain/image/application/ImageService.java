package project.volunteer.domain.image.application;

import project.volunteer.domain.image.application.dto.SaveImageDto;

public interface ImageService {

    public Long addImage(SaveImageDto saveImageDto);
}
