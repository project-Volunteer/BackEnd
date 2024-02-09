package project.volunteer.domain.image.application;

import java.time.LocalTime;
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
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.global.common.component.Address;
import project.volunteer.global.common.component.Coordinate;
import project.volunteer.global.common.component.RealWorkCode;
import project.volunteer.domain.recruitment.application.RecruitmentCommandUseCase;
import project.volunteer.domain.recruitment.application.dto.command.RecruitmentCreateCommand;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.image.domain.Storage;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.domain.Gender;
import project.volunteer.domain.user.domain.Role;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.HourFormat;
import project.volunteer.global.common.component.IsDeleted;
import project.volunteer.global.common.component.Timetable;
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
    @Autowired
    RecruitmentCommandUseCase recruitmentService;
    @Autowired ImageService imageService;
    @Autowired ImageRepository imageRepository;
    @Autowired FileService fileService;

    User writer;
    private Long saveRecruitmentNo;
    private List<Long> uploadImageNoList = new ArrayList<>();
    private void clear() {
        em.flush();
        em.clear();
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
    private void setUploadImage(RealWorkCode realWorkCode, Long no) throws IOException {
        ImageParam imageParam = ImageParam.builder()
                .code(realWorkCode)
                .no(no)
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