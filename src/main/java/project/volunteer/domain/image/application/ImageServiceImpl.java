package project.volunteer.domain.image.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.image.dao.ImageRepository;
import project.volunteer.domain.image.domain.Image;
import project.volunteer.global.common.component.RealWorkCode;
import project.volunteer.domain.image.application.dto.ImageParam;
import project.volunteer.domain.image.domain.Storage;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService{

    private final StorageService storageService;
    private final ImageRepository imageRepository;

    @Transactional
    @Override
    //TODO: 퍼사드 패턴 적용하기
    public Long addImage(ImageParam saveImageDto) {
        //이미지 타입 저장
        Image createImage = Image.builder()
                .realWorkCode(saveImageDto.getCode())
                .no(saveImageDto.getNo())
                .build();

        //이미지 정보 저장
        Storage uploadImageStorage = storageService.addStorage(saveImageDto.getUploadImage(), saveImageDto.getCode());
        createImage.setStorage(uploadImageStorage);

        return imageRepository.save(createImage).getImageNo();
    }

    @Override
    @Transactional
    public void deleteImage(RealWorkCode code, Long no) {
        imageRepository.findByCodeAndNo(code, no)
                .ifPresent(img -> img.setDeleted());
        /**
         * 추후 이미지 용량이 많이 쌓일 경우 Spring batch 를 통해서 해결해보기!
         */
    }

    @Override
    @Transactional
    public void deleteImageList(RealWorkCode code, Long no) {
        imageRepository.findImagesByCodeAndNo(code, no).stream()
                .forEach(img -> img.setDeleted());
    }
}
