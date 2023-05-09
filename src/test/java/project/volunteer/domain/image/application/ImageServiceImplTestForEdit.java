package project.volunteer.domain.image.application;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.image.application.dto.ImageParam;
import project.volunteer.domain.image.dao.ImageRepository;
import project.volunteer.domain.image.domain.Image;
import project.volunteer.domain.image.domain.ImageType;
import project.volunteer.domain.image.domain.RealWorkCode;
import project.volunteer.domain.recruitment.application.RecruitmentService;
import project.volunteer.domain.recruitment.application.dto.RecruitmentParam;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.storage.domain.Storage;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.domain.Gender;
import project.volunteer.domain.user.domain.Role;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.HourFormat;
import project.volunteer.global.common.component.IsDeleted;
import project.volunteer.global.infra.s3.FileService;

import javax.persistence.EntityManager;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@SpringBootTest
@Transactional
class ImageServiceImplTestForEdit {

    @Autowired EntityManager em;
    @Autowired UserRepository userRepository;
    @Autowired RecruitmentService recruitmentService;
    @Autowired ImageService imageService;
    @Autowired ImageRepository imageRepository;
    @Autowired FileService fileService;

    private Long saveRecruitmentNo;
    private List<Long> uploadImageNoList = new ArrayList<>();
    private void clear() {
        em.flush();
        em.clear();
    }
    private void setRecruitment(){
        String category = "001";
        String organizationName ="name";
        String sido = "11";
        String sigungu = "11011";
        String details = "details";
        Float latitude = 3.2F , longitude = 3.2F;
        Boolean isIssued = true;
        String volunteerType = "1"; //all
        Integer volunteerNum = 5;
        String volunteeringType = VolunteeringType.IRREG.name();
        String startDay = "01-01-2000";
        String endDay = "01-01-2000";
        String hourFormat = HourFormat.AM.name();
        String startTime = "01:01";
        Integer progressTime = 3;
        String title = "title", content = "content";
        Boolean isPublished = true;
        RecruitmentParam saveRecruitDto = new RecruitmentParam(category, organizationName, sido,sigungu, details, latitude, longitude,
                isIssued, volunteerType, volunteerNum, volunteeringType, startDay, endDay, hourFormat, startTime, progressTime, title, content, isPublished);
        saveRecruitmentNo = recruitmentService.addRecruitment(saveRecruitDto);
    }
    private void setUploadImage(RealWorkCode realWorkCode, Long no) throws IOException {
        ImageParam imageParam = ImageParam.builder()
                .code(realWorkCode)
                .imageType(ImageType.UPLOAD)
                .no(no)
                .staticImageCode(null)
                .uploadImage(getMockMultipartFile())
                .build();
        uploadImageNoList.add(imageService.addImage(imageParam));
    }
    private MockMultipartFile getMockMultipartFile() throws IOException {
        return new MockMultipartFile(
                "file", "file.PNG", "image/jpg", new FileInputStream("src/main/resources/static/test/file.PNG"));
    }
    @BeforeEach
    private void initUser() {
        userRepository.save(User.builder()
                .id("1234")
                .password("1234")
                .nickName("nickname")
                .email("email@gmail.com")
                .gender(Gender.M)
                .birthDay(LocalDate.now())
                .picture("picture")
                .joinAlarmYn(true).beforeAlarmYn(true).noticeAlarmYn(true)
                .role(Role.USER)
                .provider("kakao").providerId("1234")
                .build());
        clear();
    }
    @AfterEach
    public void deleteS3Image() { //S3에 테스트를 위해 저장한 이미지 삭제
        for(Long id : uploadImageNoList){
            Image image = imageRepository.findById(id).get();
            Storage storage = image.getStorage();
            fileService.deleteFile(storage.getFakeImageName());
        }
    }

    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 단건_이미지_삭제_성공() throws IOException {
        //given
        setRecruitment();
        setUploadImage(RealWorkCode.RECRUITMENT, saveRecruitmentNo);
        clear();

        //when
        imageService.deleteImage(RealWorkCode.RECRUITMENT, saveRecruitmentNo);

        //then
        uploadImageNoList.stream()
                .forEach(img -> {
                    Image image = imageRepository.findById(img).get();
                    Assertions.assertThat(image.getIsDeleted()).isEqualTo(IsDeleted.Y);
                    Assertions.assertThat(image.getStorage().getIsDeleted()).isEqualTo(IsDeleted.Y);
                });
    }

    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 복수_이미지_삭제_성공() throws IOException {
        //given
        setRecruitment();
        setUploadImage(RealWorkCode.RECRUITMENT, saveRecruitmentNo);
        setUploadImage(RealWorkCode.RECRUITMENT, saveRecruitmentNo);
        setUploadImage(RealWorkCode.RECRUITMENT, saveRecruitmentNo);
        clear();

        //when
        imageService.deleteImageList(RealWorkCode.RECRUITMENT, saveRecruitmentNo);

        //then
        uploadImageNoList.stream()
                .forEach(img -> {
                    Image image = imageRepository.findById(img).get();
                    Assertions.assertThat(image.getIsDeleted()).isEqualTo(IsDeleted.Y);
                    Assertions.assertThat(image.getStorage().getIsDeleted()).isEqualTo(IsDeleted.Y);
                });
    }

}