package project.volunteer.domain.image.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.image.dao.ImageRepository;
import project.volunteer.domain.image.domain.Image;
import project.volunteer.domain.image.domain.ImageType;
import project.volunteer.domain.image.domain.RealWorkCode;
import project.volunteer.domain.image.application.dto.ImageParam;
import project.volunteer.domain.recruitment.dao.RecruitmentRepository;
import project.volunteer.domain.storage.application.StorageService;
import project.volunteer.domain.storage.domain.Storage;
import project.volunteer.domain.user.dao.UserRepository;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService{

    private final StorageService storageService;
    private final ImageRepository imageRepository;
    private final UserRepository userRepository;
    private final RecruitmentRepository recruitmentRepository;

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


    private void validateNo(Long no, RealWorkCode code) {

        //더 클린하게 작성할 수 없을까?
        if(code==RealWorkCode.USER) {
            userRepository.findById(no).orElseThrow(() -> new NullPointerException(String.format("Not found userNo=[%d]",no)));
        }else if(code==RealWorkCode.RECRUITMENT){
            recruitmentRepository.findById(no).orElseThrow(() -> new NullPointerException(String.format("Not found recruitmentNo=[%d]",no)));
        }
    }

}
