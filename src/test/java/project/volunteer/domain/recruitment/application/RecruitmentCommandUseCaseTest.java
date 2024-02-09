package project.volunteer.domain.recruitment.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.given;

import com.amazonaws.services.kms.model.NotFoundException;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import project.volunteer.domain.image.domain.Image;
import project.volunteer.domain.recruitment.application.dto.command.RecruitmentCreateCommand;
import project.volunteer.domain.recruitment.application.dto.command.RepeatPeriodCreateCommand;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.recruitment.domain.repeatPeriod.Day;
import project.volunteer.domain.recruitment.domain.repeatPeriod.Period;
import project.volunteer.domain.recruitment.domain.repeatPeriod.Week;
import project.volunteer.domain.user.domain.Gender;
import project.volunteer.domain.user.domain.Role;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.Address;
import project.volunteer.global.common.component.Coordinate;
import project.volunteer.global.common.component.HourFormat;
import project.volunteer.global.common.component.RealWorkCode;
import project.volunteer.global.common.component.Timetable;
import project.volunteer.global.infra.s3.FileFolder;
import project.volunteer.support.ServiceTest;

class RecruitmentCommandUseCaseTest extends ServiceTest {
    private final Address address = new Address("111", "11", "test", "test");
    private final Coordinate coordinate = new Coordinate(1.2F, 2.2F);
    private final Timetable timetable = new Timetable(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1), HourFormat.AM,
            LocalTime.now(), 10);
    private final User user = new User("test", "test", "test", "test@email.com", Gender.M, LocalDate.now(),
            "http://...", true, true, true, Role.USER, "kakao", "1234", null);

    @BeforeEach
    void setUp() {
        userRepository.save(user);
    }

    @DisplayName("봉사 모집글을 생성하고 저장한다.")
    @Test
    void addRecruitment() {
        // given
        final RecruitmentCreateCommand command = createCommand(VolunteeringCategory.ADMINSTRATION_ASSISTANCE,
                VolunteeringType.IRREG, VolunteerType.ADULT, 50, null, true, null);

        // when
        final Long recruitmentNo = recruitmentCommandUseCase.addRecruitment(user, command);

        // then
        Recruitment findRecruitment = findRecruitmentBy(recruitmentNo);
        assertAll(
                () -> assertThat(findRecruitment.getVolunteeringCategory()).isEqualByComparingTo(
                        command.getVolunteeringCategory()),
                () -> assertThat(findRecruitment.getVolunteeringType()).isEqualByComparingTo(
                        command.getVolunteeringType()),
                () -> assertThat(findRecruitment.getVolunteerType()).isEqualByComparingTo(command.getVolunteerType()),
                () -> assertThat(findRecruitment.getMaxParticipationNum()).isEqualTo(command.getMaxParticipationNum()),
                () -> assertThat(findRecruitment.getCurrentVolunteerNum()).isEqualTo(0)
        );
    }

    @DisplayName("매주 주기적으로 반복하는 봉사 모집글을 생성하고 저장한다.")
    @Test
    void saveWeeklyRegularRecruitment() {
        //given
        final RecruitmentCreateCommand command = createCommand(VolunteeringCategory.ADMINSTRATION_ASSISTANCE,
                VolunteeringType.REG, VolunteerType.ADULT, 50,
                new RepeatPeriodCreateCommand(Period.WEEK, Week.NONE, List.of(Day.MON, Day.TUES)),
                true, null);

        //when
        final Long recruitmentNo = recruitmentCommandUseCase.addRecruitment(user, command);

        //then
        assertThat(repeatPeriodRepository.findByRecruitment_RecruitmentNo(recruitmentNo)).hasSize(2)
                .extracting("period", "week", "day")
                .containsExactlyInAnyOrder(
                        tuple(command.getRepeatPeriodCommand().getPeriod(), command.getRepeatPeriodCommand().getWeek(),
                                command.getRepeatPeriodCommand().getDayOfWeeks().get(0)),
                        tuple(command.getRepeatPeriodCommand().getPeriod(), command.getRepeatPeriodCommand().getWeek(),
                                command.getRepeatPeriodCommand().getDayOfWeeks().get(1))
                );
        assertThat(scheduleRepository.findByRecruitment_RecruitmentNo(recruitmentNo)).hasSize(10);
    }

    @DisplayName("봉사 모집글 이미지를 성공적으로 저장한다.")
    @Test
    void saveRecruitmentWithUploadImage() throws IOException {
        //given
        final String expectedOriginalFileName = "test-image.PNG";
        final String expectedSavedFileName = "recruitment/" + expectedOriginalFileName;
        final String expectedSavedFilePath = "http://s3...";
        final MockMultipartFile imageFile = getMockMultipartFile(expectedOriginalFileName);
        final RecruitmentCreateCommand command = createCommand(VolunteeringCategory.ADMINSTRATION_ASSISTANCE,
                VolunteeringType.REG, VolunteerType.ADULT, 50,
                new RepeatPeriodCreateCommand(Period.WEEK, Week.NONE, List.of(Day.MON, Day.TUES)),
                false, imageFile);

        given(fileService.uploadFile(imageFile, FileFolder.RECRUITMENT_IMAGES))
                .willReturn(expectedSavedFileName);
        given(fileService.getFileUrl(expectedSavedFileName)).willReturn(expectedSavedFilePath);

        //when
        Long recruitmentNo = recruitmentCommandUseCase.addRecruitment(user, command);

        //then
        Image image = imageRepository.findByCodeAndNo(RealWorkCode.RECRUITMENT, recruitmentNo)
                .orElseThrow();
        assertAll(
                () -> assertThat(image.getStorage().getRealImageName()).isEqualTo(expectedOriginalFileName),
                () -> assertThat(image.getStorage().getFakeImageName()).isEqualTo(expectedSavedFileName),
                () -> assertThat(image.getStorage().getImagePath()).isEqualTo(expectedSavedFilePath)
        );
    }

    private RecruitmentCreateCommand createCommand(VolunteeringCategory category, VolunteeringType volunteeringType,
                                                   VolunteerType volunteerType, int maxParticipationNum,
                                                   RepeatPeriodCreateCommand repeatPeriodCreateCommand,
                                                   Boolean isStaticImage, MultipartFile uploadImageFile) {
        return new RecruitmentCreateCommand("title", "content", category,
                volunteeringType, volunteerType, maxParticipationNum, true, "organization", true,
                address, coordinate, timetable, repeatPeriodCreateCommand, isStaticImage, uploadImageFile);
    }

    private Recruitment findRecruitmentBy(Long recruitmentNo) {
        return recruitmentRepository.findById(recruitmentNo)
                .orElseThrow(() -> new NotFoundException("모집글이 존재하지 않습니다."));
    }

    private MockMultipartFile getMockMultipartFile(String fileName) throws IOException {
        return new MockMultipartFile(
                "file", fileName, "image/jpg", new FileInputStream("src/main/resources/static/test/file.PNG"));
    }

}