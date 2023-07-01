package project.volunteer.domain.image.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.image.dao.ImageRepository;
import project.volunteer.domain.image.domain.Image;
import project.volunteer.domain.image.domain.ImageType;
import project.volunteer.domain.image.domain.RealWorkCode;
import project.volunteer.domain.logboard.dao.LogboardRepository;
import project.volunteer.domain.image.application.dto.ImageParam;
import project.volunteer.domain.recruitment.dao.RecruitmentRepository;
import project.volunteer.domain.storage.application.StorageService;
import project.volunteer.domain.storage.domain.Storage;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService{

    private final StorageService storageService;
    private final ImageRepository imageRepository;
    private final UserRepository userRepository;
    private final RecruitmentRepository recruitmentRepository;
    private final LogboardRepository logboardRepository;

    @Transactional
    @Override
    public Long addImage(ImageParam saveImageDto) {

        //참조 엔티티 유무 검사필요(user, recruitment, log...)
        validateNo(saveImageDto.getNo(), saveImageDto.getCode());

        Image createImage = Image.builder()
                .realWorkCode(saveImageDto.getCode())
                .no(saveImageDto.getNo())
                .staticImageName(saveImageDto.getStaticImageCode())
                .build();

        //업로드 이미지일 경우
        if(saveImageDto.getImageType()== ImageType.UPLOAD){
            Storage uploadImageStorage = storageService.addStorage(saveImageDto.getUploadImage(), saveImageDto.getCode());
            createImage.setStorage(uploadImageStorage);
        }

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

    private void validateNo(Long no, RealWorkCode code) {

        //더 클린하게 작성할 수 없을까?
        if(code==RealWorkCode.USER) {
            userRepository.findById(no).orElseThrow(() ->
                    new BusinessException(ErrorCode.NOT_EXIST_USER, String.format("User No = [%d]", no)));
        }else if(code==RealWorkCode.RECRUITMENT){
            recruitmentRepository.findById(no).orElseThrow(() ->
                    new BusinessException(ErrorCode.NOT_EXIST_RECRUITMENT, String.format("Recruitment No = [%d]", no)));
        }else if(code==RealWorkCode.LOG) {
        	 logboardRepository.findById(no).orElseThrow(() ->
                    new BusinessException(ErrorCode.NOT_EXIST_LOGBOARD, String.format("Logboard No = [%d]", no)));
        }
    }

}
