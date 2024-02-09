package project.volunteer.domain.image.application;

import java.time.LocalTime;
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
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.global.common.component.Address;
import project.volunteer.global.common.component.Coordinate;
import project.volunteer.global.common.component.RealWorkCode;
import project.volunteer.domain.image.application.dto.ImageParam;
import project.volunteer.domain.recruitment.application.RecruitmentCommandUseCase;
import project.volunteer.domain.recruitment.application.dto.command.RecruitmentCreateCommand;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.image.domain.Storage;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.domain.Gender;
import project.volunteer.domain.user.domain.Role;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.HourFormat;
import project.volunteer.global.common.component.Timetable;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.infra.s3.FileService;

import javax.persistence.EntityManager;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;

@SpringBootTest
@Transactional
class ImageServiceImplTestForSave {

    @Autowired EntityManager em;
    @Autowired UserRepository userRepository;
    @Autowired
    RecruitmentCommandUseCase recruitmentService;
    @Autowired ImageService imageService;
    @Autowired ImageRepository imageRepository;
    @Autowired FileService fileService;

    User writer;
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
        final Address address = new Address("111", "11", "test", "test");
        final Coordinate coordinate = new Coordinate(1.2F, 2.2F);
        final Timetable timetable = new Timetable(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1), HourFormat.AM,
                LocalTime.now(), 10);
        final RecruitmentCreateCommand saveRecruitDto = new RecruitmentCreateCommand("title", "content", VolunteeringCategory.EDUCATION,
                VolunteeringType.IRREG, VolunteerType.ADULT, 10, true, "organization", true,
                address, coordinate, timetable, null, true, null);
        saveRecruitmentNo = recruitmentService.addRecruitment(writer, saveRecruitDto);
    }
    @BeforeEach
    private void initUser() {
        writer = userRepository.save(User.builder()
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
    }

    @Test
    @WithUserDetails(value = "1234", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    public void 모집글_이미지_저장_성공() throws IOException {
        //init
        setRecruitment();

        //given
        ImageParam dto = ImageParam.builder()
                .code(RealWorkCode.RECRUITMENT)
                .no(saveRecruitmentNo)
                .uploadImage(getMockMultipartFile())
                .build();

        //when
        Long saveId = imageService.addImage(dto);
        clear();

        //then
        Image image = imageRepository.findById(saveId).get();
        Assertions.assertThat(image.getImageNo()).isEqualTo(saveId);
        Assertions.assertThat(image.getNo()).isEqualTo(saveRecruitmentNo);
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
                .no(saveRecruitmentNo)
                .uploadImage(getFailMockMultipartFile())
                .build();

        //when & then
        Assertions.assertThatThrownBy(() -> imageService.addImage(dto))
                .isInstanceOf(BusinessException.class);
    }
}