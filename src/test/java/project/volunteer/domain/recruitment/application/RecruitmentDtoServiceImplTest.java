package project.volunteer.domain.recruitment.application;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.image.application.ImageService;
import project.volunteer.domain.image.application.dto.ImageParam;
import project.volunteer.domain.image.dao.ImageRepository;
import project.volunteer.domain.image.domain.Image;
import project.volunteer.global.common.component.RealWorkCode;
import project.volunteer.domain.participation.dao.ParticipantRepository;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.dao.RepeatPeriodRepository;
import project.volunteer.domain.recruitment.domain.Period;
import project.volunteer.domain.recruitment.domain.RepeatPeriod;
import project.volunteer.global.common.component.*;
import project.volunteer.domain.recruitment.application.dto.RecruitmentDetails;
import project.volunteer.domain.recruitment.dao.RecruitmentRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.recruitment.domain.Day;
import project.volunteer.domain.recruitment.domain.Week;
import project.volunteer.domain.image.domain.Storage;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.domain.Gender;
import project.volunteer.domain.user.domain.Role;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.infra.s3.FileService;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
class RecruitmentDtoServiceImplTest {

    @PersistenceContext EntityManager em;
    @Autowired UserRepository userRepository;
    @Autowired
    RecruitmentQueryUseCase recruitmentDtoService;
    @Autowired RepeatPeriodRepository repeatPeriodRepository;
    @Autowired ImageService imageService;
    @Autowired FileService fileService;
    @Autowired ImageRepository imageRepository;
    @Autowired ParticipantRepository participantRepository;
    @Autowired RecruitmentRepository recruitmentRepository;

    private Recruitment saveRecruitment;
    private RepeatPeriod saveRegPeriod;
    private User writer;
    private List<Long> deleteImageNo = new ArrayList<>();

    @BeforeEach
    private void setUp() throws IOException {

        writer = 작성자_등록();
        saveRecruitment = 정기모집글_등록(writer);
        업로드_이미지_등록(RealWorkCode.RECRUITMENT, saveRecruitment.getRecruitmentNo());
        업로드_이미지_등록(RealWorkCode.USER, writer.getUserNo());
    }
    @AfterEach
    public void deleteS3Image() { //S3에 테스트를 위해 저장한 이미지 삭제
        for(Long id : deleteImageNo){
            Image image = imageRepository.findById(id).get();
            Storage storage = image.getStorage();
            fileService.deleteFile(storage.getFakeImageName());
        }
    }
    private void clear() {
        em.flush();
        em.clear();
    }
    private User 작성자_등록(){
        User writer = User.createUser("1234", "1234", "1234", "1234", Gender.M, LocalDate.now(), "1234",
                true, true, true, Role.USER, "kakao", "1234", null);
        return userRepository.save(writer);
    }
    private Recruitment 정기모집글_등록(User writer) {
        //모집글 저장
        final String title = "title";
        final String content = "content";
        final VolunteeringCategory category = VolunteeringCategory.EDUCATION;
        final VolunteeringType volunteeringType = VolunteeringType.REG;
        final VolunteerType volunteerType = VolunteerType.TEENAGER;
        final int volunteerNum = 4;
        final Boolean isIssued = true;
        final  String organizationName = "name";
        final Address address = Address.createAddress("1", "111", "details", "fullName");
        final Coordinate coordinate = Coordinate.createCoordinate(3.2F, 3.2F);
        final Timetable timetable = Timetable.createTimetable(LocalDate.now(), LocalDate.now().plusMonths(3), HourFormat.AM, LocalTime.now(), 3);
        final  Boolean isPublished = true;

        Recruitment createRecruitment = Recruitment.createRecruitment(title, content, category, volunteeringType, volunteerType, volunteerNum, isIssued, organizationName,
                address, coordinate, timetable, isPublished);
        createRecruitment.setWriter(writer);
        Recruitment save = recruitmentRepository.save(createRecruitment);

        //반복 주기 저장(정기 모집글)
        RepeatPeriod period = RepeatPeriod.createRepeatPeriod(Period.MONTH, Week.FIRST, Day.MON);
        period.setRecruitment(save);
        this.saveRegPeriod = repeatPeriodRepository.save(period);
        return save;
    }
    private void 업로드_이미지_등록(RealWorkCode realWorkCode, Long no) throws IOException {
        ImageParam imageDto = ImageParam.builder()
                .code(realWorkCode)
                .no(no)
                .uploadImage(getMockMultipartFile())
                .build();
        Long imageNo = imageService.addImage(imageDto);
        deleteImageNo.add(imageNo);
    }
    private MockMultipartFile getMockMultipartFile() throws IOException {
        return new MockMultipartFile(
                "file", "file.PNG", "image/jpg", new FileInputStream("src/main/resources/static/test/file.PNG"));
    }

    @DisplayName("봉사 모집글 상세조회에 성공하다.")
    @Test
    @Transactional
    public void 모집글_상세조회_성공() throws IOException {
        //given & when
        RecruitmentDetails details = recruitmentDtoService.findRecruitmentAndWriterDto(saveRecruitment.getRecruitmentNo());
        clear();

        //then
        assertAll(
                () -> assertThat(details.getVolunteeringCategory()).isEqualTo(saveRecruitment.getVolunteeringCategory().getId()),
                () -> assertThat(details.getOrganizationName()).isEqualTo(saveRecruitment.getOrganizationName()),
                () -> assertThat(details.getIsIssued()).isEqualTo(saveRecruitment.getIsIssued()),
                () -> assertThat(details.getVolunteerType()).isEqualTo(saveRecruitment.getVolunteerType().getId()),
                () -> assertThat(details.getVolunteerNum()).isEqualTo(saveRecruitment.getVolunteerNum()),
                () -> assertThat(details.getVolunteeringType()).isEqualTo(saveRecruitment.getVolunteeringType().getId()),
                () -> assertThat(details.getTitle()).isEqualTo(saveRecruitment.getTitle()),
                () -> assertThat(details.getContent()).isEqualTo(saveRecruitment.getContent()),
                () -> assertThat(details.getAuthor().getNickName()).isEqualTo(writer.getNickName())
        );
    }

    @Test
    @Transactional
    public void 모집글_상세조회_실패_삭제된게시물() throws IOException {
        //given
        saveRecruitment.setDeleted();
        clear();

        //when & then
        Assertions.assertThatThrownBy(() -> recruitmentDtoService.findRecruitmentAndWriterDto(saveRecruitment.getRecruitmentNo()))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @Transactional
    public void 모집글_상세조회_실패_임시게시물() throws IOException {
        //given
        saveRecruitment.setIsPublished(false); //임시 게시물로 만들기
        clear();

        //when & then
        Assertions.assertThatThrownBy(() -> recruitmentDtoService.findRecruitmentAndWriterDto(saveRecruitment.getRecruitmentNo()))
                .isInstanceOf(BusinessException.class);
    }

}