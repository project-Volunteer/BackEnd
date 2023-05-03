package project.volunteer.domain.image.application;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.image.dao.ImageRepository;
import project.volunteer.domain.image.domain.Image;
import project.volunteer.domain.image.domain.ImageType;
import project.volunteer.domain.image.domain.RealWorkCode;
import project.volunteer.domain.image.application.dto.ImageParam;
import project.volunteer.domain.recruitment.application.RecruitmentService;
import project.volunteer.domain.recruitment.application.dto.RecruitmentParam;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.storage.domain.Storage;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.domain.Gender;
import project.volunteer.domain.user.domain.Role;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.HourFormat;
import project.volunteer.global.error.exception.BaseException;
import project.volunteer.global.infra.s3.FileService;

import javax.persistence.EntityManager;
import javax.persistence.SecondaryTable;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;

@SpringBootTest
@Transactional
class ImageServiceImplTestForSave {

    @Autowired EntityManager em;
    @Autowired UserRepository userRepository;
    @Autowired RecruitmentService recruitmentService;
    @Autowired ImageService imageService;
    @Autowired ImageRepository imageRepository;
    @Autowired FileService fileService;

    private Long saveRecruitmentNo;
    private void clear() {
        em.flush();
        em.clear();
    }
    private MockMultipartFile getMockMultipartFile() throws IOException {
        return new MockMultipartFile(
                "file", "file.PNG", "image/jpg", new FileInputStream("src/main/resources/static/test/file.PNG"));
    }
    private MockMultipartFile getFailMockMultipartFile() throws IOException {
        return new MockMultipartFile(
                "file", "filejpg", "image/jpg", new FileInputStream("src/main/resources/static/test/file.PNG"));
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
        clear();
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

    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 모집글_정적_이미지_저장_실패_존재하지않는모집글() {
        //init
        setRecruitment();

        //given
        ImageParam dto = ImageParam.builder()
                .code(RealWorkCode.RECRUITMENT)
                .imageType(ImageType.STATIC)
                .no(Long.MAX_VALUE) //-> 없는 모집글 PK
                .staticImageCode("1")
                .uploadImage(null)
                .build();

        //when,then
        Assertions.assertThatThrownBy(() -> imageService.addImage(dto))
                .isInstanceOf(BaseException.class);
    }

    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 사용자_정적_이미지_저장_실패_존재하지않는사용자(){
        //given
        ImageParam dto = ImageParam.builder()
                .code(RealWorkCode.USER)
                .imageType(ImageType.STATIC)
                .no(Long.MAX_VALUE) //-> 없는 모집글 PK
                .staticImageCode("1")
                .uploadImage(null)
                .build();

        //when & then
        Assertions.assertThatThrownBy(() -> imageService.addImage(dto))
                .isInstanceOf(BaseException.class);
    }

    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 모집글_정적_이미지_저장_성공() throws IOException {
        //init
        setRecruitment();

        //given
        ImageParam dto = ImageParam.builder()
                .code(RealWorkCode.RECRUITMENT)
                .imageType(ImageType.STATIC)
                .no(saveRecruitmentNo)
                .staticImageCode("1")
                .uploadImage(null)
                .build();

        //when
        Long saveId = imageService.addImage(dto);
        clear();

        //then
        Image image = imageRepository.findById(saveId).get();
        Assertions.assertThat(image.getImageNo()).isEqualTo(saveId);
        Assertions.assertThat(image.getNo()).isEqualTo(saveRecruitmentNo);
        Assertions.assertThat(image.getStaticImageName()).isEqualTo("1");
        Assertions.assertThat(image.getRealWorkCode()).isEqualTo(RealWorkCode.RECRUITMENT);
        Assertions.assertThat(image.getStorage()).isNull();
    }

    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 모집글_업로드_이미지_저장_성공() throws IOException {
        //init
        setRecruitment();

        //given
        ImageParam dto = ImageParam.builder()
                .code(RealWorkCode.RECRUITMENT)
                .imageType(ImageType.UPLOAD)
                .no(saveRecruitmentNo)
                .staticImageCode(null)
                .uploadImage(getMockMultipartFile())
                .build();

        //when
        Long saveId = imageService.addImage(dto);
        clear();

        //then
        Image image = imageRepository.findById(saveId).get();
        Assertions.assertThat(image.getImageNo()).isEqualTo(saveId);
        Assertions.assertThat(image.getNo()).isEqualTo(saveRecruitmentNo);
        Assertions.assertThat(image.getStaticImageName()).isNull();
        Assertions.assertThat(image.getRealWorkCode()).isEqualTo(RealWorkCode.RECRUITMENT);

        Storage storage = image.getStorage();
        Assertions.assertThat(storage.getRealImageName()).isEqualTo("file.PNG");
        Assertions.assertThat(storage.getExtName()).isEqualTo(".PNG");

        //finally
        fileService.deleteFile(storage.getFakeImageName());
    }

    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 모집글_업로드_이미지_실패_잘못된확장자() throws IOException {
        //given
        setRecruitment();
        ImageParam dto = ImageParam.builder()
                .code(RealWorkCode.RECRUITMENT)
                .imageType(ImageType.UPLOAD)
                .no(saveRecruitmentNo)
                .staticImageCode(null)
                .uploadImage(getFailMockMultipartFile())
                .build();

        //when & then
        Assertions.assertThatThrownBy(() -> imageService.addImage(dto))
                .isInstanceOf(BaseException.class);
    }

}