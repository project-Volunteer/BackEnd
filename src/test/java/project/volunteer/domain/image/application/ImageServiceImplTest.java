package project.volunteer.domain.image.application;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.image.dao.ImageRepository;
import project.volunteer.domain.image.domain.Image;
import project.volunteer.domain.image.domain.ImageType;
import project.volunteer.domain.image.domain.RealWorkCode;
import project.volunteer.domain.image.application.dto.ImageParam;
import project.volunteer.domain.recruitment.application.RecruitmentService;
import project.volunteer.domain.recruitment.application.dto.RecruitmentParam;
import project.volunteer.domain.storage.domain.Storage;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.domain.Gender;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.infra.s3.FileService;

import javax.persistence.EntityManager;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;

@SpringBootTest
@Transactional
class ImageServiceImplTest {

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
    @BeforeEach
    private void signUpAndSetAuthenticationAndSaveRecruitment() {

        String nickname = "nickname";
        String email = "email@gmail.com";
        Gender gender = Gender.M;
        LocalDate birth = LocalDate.now();
        String picture = "picture";
        Boolean alarm = true;
        userRepository.save(User.builder().nickName(nickname)
                .email(email).gender(gender).birthDay(birth).picture(picture)
                .joinAlarmYn(alarm).beforeAlarmYn(alarm).noticeAlarmYn(alarm).build());

        SecurityContext emptyContext = SecurityContextHolder.createEmptyContext();
        emptyContext.setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        new org.springframework.security.core.userdetails.User(
                                email,"temp",new ArrayList<>())
                        , null
                )
        );
        SecurityContextHolder.setContext(emptyContext);

        String category = "001";
        String organizationName ="name";
        String sido = "11";
        String sigungu = "11011";
        String details = "details";
        Float latitude = 3.2F , longitude = 3.2F;
        Boolean isIssued = true;
        String volunteerType = "1"; //all
        Integer volunteerNum = 5;
        String volunteeringType = "short";
        String startDay = "01-01-2000";
        String endDay = "01-01-2000";
        String startTime = "01:01:00";
        Integer progressTime = 3;
        String title = "title", content = "content";
        Boolean isPublished = true;
        RecruitmentParam saveRecruitDto = new RecruitmentParam(category, organizationName, sido,sigungu, details, latitude, longitude,
                isIssued, volunteerType, volunteerNum, volunteeringType, startDay, endDay, startTime, progressTime, title, content, isPublished);
        saveRecruitmentNo = recruitmentService.addRecruitment(saveRecruitDto);

        clear();
    }

    @Test
    public void 모집글_이미지_저장_실패_없는모집글PK() {
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
                .isInstanceOf(NullPointerException.class);
    }
    @Test
    public void 모집글_정적_이미지_저장_성공() throws IOException {
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
    public void 모집글_업로드_이미지_저장_성공() throws IOException {
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

}