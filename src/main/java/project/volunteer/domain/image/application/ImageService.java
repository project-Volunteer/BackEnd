package project.volunteer.domain.image.application;

import project.volunteer.domain.image.application.dto.SaveImageDto;
import project.volunteer.domain.image.domain.Image;
import project.volunteer.domain.image.domain.RealWorkCode;

import java.util.Optional;

public interface ImageService {

    public Long addImage(SaveImageDto saveImageDto);
}
