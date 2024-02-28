package project.volunteer.domain.recruitment.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.given;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import project.volunteer.domain.image.domain.Image;
import project.volunteer.domain.image.domain.Storage;
import project.volunteer.domain.recruitmentParticipation.domain.RecruitmentParticipation;
import project.volunteer.domain.recruitment.application.dto.query.detail.RecruitmentDetailSearchResult;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.recruitment.domain.repeatPeriod.Day;
import project.volunteer.domain.recruitment.domain.repeatPeriod.Period;
import project.volunteer.domain.recruitment.domain.repeatPeriod.RepeatPeriod;
import project.volunteer.domain.recruitment.domain.repeatPeriod.Week;
import project.volunteer.domain.user.domain.Gender;
import project.volunteer.domain.user.domain.Role;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.Address;
import project.volunteer.global.common.component.Coordinate;
import project.volunteer.global.common.component.HourFormat;
import project.volunteer.global.common.component.IsDeleted;
import project.volunteer.global.common.component.ParticipantState;
import project.volunteer.global.common.component.RealWorkCode;
import project.volunteer.global.common.component.Timetable;
import project.volunteer.global.common.dto.StateResult;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;
import project.volunteer.support.ServiceTest;

class RecruitmentQueryUseCaseTest extends ServiceTest {
    private final Address address = new Address("111", "11", "test", "test");
    private final Coordinate coordinate = new Coordinate(1.2F, 2.2F);
    private final Timetable timetable = new Timetable(LocalDate.now(), LocalDate.now(), HourFormat.AM, LocalTime.now(),
            10);

    @DisplayName("봉사 모집글 정보를 상세조회 한다.")
    @Test
    void searchRecruitmentDetail() {
        //given
        final String recruitmentUploadImagePath = "http://s3-recruitment...";
        final String userUploadImagePath1 = "http://s3-user1...";
        final String userUploadImagePath2 = "http://s3-user2...";

        User writer = createAndSaveUser("writer", "http://writer...");
        Recruitment recruitment = createAndSaveRecruitment(writer, VolunteeringType.REG,
                List.of(new RepeatPeriod(Period.WEEK, Week.NONE, Day.MON, null, IsDeleted.N),
                        new RepeatPeriod(Period.WEEK, Week.NONE, Day.TUES, null, IsDeleted.N)));
        createAndSaveUploadImage(RealWorkCode.RECRUITMENT, recruitment.getRecruitmentNo(), recruitmentUploadImagePath);

        User user1 = createAndSaveUser("user1", "http://user1...");
        User user2 = createAndSaveUser("user2", "http://user2...");
        createAndSaveUploadImage(RealWorkCode.USER, user1.getUserNo(), userUploadImagePath1);
        createAndSaveUploadImage(RealWorkCode.USER, user2.getUserNo(), userUploadImagePath2);
        RecruitmentParticipation participant1 = recruitmentParticipationRepository.save(
                new RecruitmentParticipation(recruitment, user1, ParticipantState.JOIN_REQUEST));
        RecruitmentParticipation participant2 = recruitmentParticipationRepository.save(
                new RecruitmentParticipation(recruitment, user2, ParticipantState.JOIN_APPROVAL));

        //when
        RecruitmentDetailSearchResult result = recruitmentQueryUseCase.searchRecruitmentDetail(
                recruitment.getRecruitmentNo());

        //then
        assertAll(
                () -> assertThat(result.getNo()).isEqualTo(recruitment.getRecruitmentNo()),
                () -> assertThat(result.getPicture().getIsStaticImage()).isFalse(),
                () -> assertThat(result.getPicture().getUploadImage()).isEqualTo(recruitmentUploadImagePath),
                () -> assertThat(result.getAuthor().getNickName()).isEqualTo(writer.getNickName()),
                () -> assertThat(result.getAuthor().getImageUrl()).isEqualTo(writer.getPicture()),
                () -> assertThat(result.getRepeatPeriod().getPeriod()).isEqualTo(Period.WEEK.getId()),
                () -> assertThat(result.getRepeatPeriod().getWeek()).isEqualTo(Week.NONE.getId()),
                () -> assertThat(result.getRepeatPeriod().getDayOfWeeks()).hasSize(2)
                        .containsExactlyInAnyOrder(Day.MON.getId(), Day.TUES.getId()),
                () -> assertThat(result.getApprovedParticipant()).hasSize(1)
                        .extracting("recruitmentParticipationNo", "nickName", "imageUrl")
                        .containsExactlyInAnyOrder(tuple(participant2.getId(), user2.getNickName(), userUploadImagePath2)),
                () -> assertThat(result.getRequiredParticipant()).hasSize(1)
                        .extracting("recruitmentParticipationNo", "nickName", "imageUrl")
                        .containsExactlyInAnyOrder(tuple(participant1.getId(), user1.getNickName(), userUploadImagePath1))
        );
    }

    @DisplayName("삭제된 봉사 모집글을 상세 조회할 경우, 예외가 발생한다.")
    @Test
    void searchRecruitmentDetailDeleted() {
        //given
        final User writer = userRepository.save(
                new User("test", "test", "test", "test@email.com", Gender.M, LocalDate.now(),
                        "http://", true, true, true, Role.USER, "kakao", "1234", null));

        final Recruitment recruitment = recruitmentRepository.save(
                new Recruitment("title1", "content1", VolunteeringCategory.ADMINSTRATION_ASSISTANCE,
                        VolunteeringType.IRREG, VolunteerType.TEENAGER, 10, 10, true, "unicef", address, coordinate,
                        timetable, 0, 0, true, IsDeleted.Y, writer));

        //when & then
        assertThatThrownBy(() -> recruitmentQueryUseCase.searchRecruitmentDetail(recruitment.getRecruitmentNo()))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.NOT_EXIST_RECRUITMENT.name());
    }

    @DisplayName("봉사 모집글 참여 가능 인원이 가득찰 경우, 요청한 회원 상태는 FULL이 된다.")
    @Test
    void searchStateWithFullRecruitment() {
        //given
        User writer = userRepository.save(
                new User("test", "test", "test", "test@email.com", Gender.M, LocalDate.now(),
                        "http://", true, true, true, Role.USER, "kakao", "1234", null));
        Recruitment recruitment = recruitmentRepository.save(
                new Recruitment("title1", "content1", VolunteeringCategory.ADMINSTRATION_ASSISTANCE,
                        VolunteeringType.IRREG, VolunteerType.TEENAGER, 10, 10, true, "unicef", address, coordinate,
                        timetable, 0, 0, true, IsDeleted.N, writer));

        //when
        StateResult stateResponse = recruitmentQueryUseCase.searchState(writer.getUserNo(),
                recruitment.getRecruitmentNo());

        //then
        assertThat(stateResponse).isEqualByComparingTo(StateResult.FULL);
    }

    @DisplayName("봉사 모집글 모집 기간이 지난 경우, 요청한 회원 상태는 DONE이 된다.")
    @Test
    void searchStateWithDoneRecruitment() {
        //given
        User writer = userRepository.save(
                new User("test", "test", "test", "test@email.com", Gender.M, LocalDate.now(),
                        "http://", true, true, true, Role.USER, "kakao", "1234", null));
        Recruitment recruitment = recruitmentRepository.save(
                new Recruitment("title1", "content1", VolunteeringCategory.ADMINSTRATION_ASSISTANCE,
                        VolunteeringType.IRREG, VolunteerType.TEENAGER, 10, 10, true, "unicef", address, coordinate,
                        new Timetable(LocalDate.of(2024, 2, 11), LocalDate.of(2024, 2, 13), HourFormat.AM,
                                LocalTime.now(), 10),
                        0, 0, true, IsDeleted.N, writer));

        given(clock.instant()).willReturn(Instant.parse("2024-02-14T10:00:00Z"));

        //when
        StateResult stateResponse = recruitmentQueryUseCase.searchState(writer.getUserNo(),
                recruitment.getRecruitmentNo());

        //then
        assertThat(stateResponse).isEqualByComparingTo(StateResult.DONE);
    }

    @DisplayName("봉사 모집글 참여 승인된 회원일 경우, 회원 상태는 APPROVED 된다.")
    @Test
    void searchStateWithApprovedRecruitment() {
        //given
        User writer = userRepository.save(
                new User("test", "test", "test", "test@email.com", Gender.M, LocalDate.now(),
                        "http://", true, true, true, Role.USER, "kakao", "1234", null));
        Recruitment recruitment = recruitmentRepository.save(
                new Recruitment("title1", "content1", VolunteeringCategory.ADMINSTRATION_ASSISTANCE,
                        VolunteeringType.IRREG, VolunteerType.TEENAGER, 10, 10, true, "unicef", address, coordinate,
                        new Timetable(LocalDate.of(2024, 2, 11), LocalDate.of(2024, 2, 13), HourFormat.AM,
                                LocalTime.now(), 10),
                        0, 0, true, IsDeleted.N, writer));

        User user = userRepository.save(
                new User("user", "user", "user", "user@email.com", Gender.M, LocalDate.now(),
                        "http://", true, true, true, Role.USER, "kakao", "4567", null));
        recruitmentParticipationRepository.save(new RecruitmentParticipation(recruitment, user, ParticipantState.JOIN_APPROVAL));

        //when
        StateResult stateResponse = recruitmentQueryUseCase.searchState(user.getUserNo(),
                recruitment.getRecruitmentNo());

        //then
        assertThat(stateResponse).isEqualByComparingTo(StateResult.APPROVED);
    }

    private Recruitment createAndSaveRecruitment(User writer, VolunteeringType volunteeringType,
                                                 List<RepeatPeriod> repeatPeriods) {
        Recruitment recruitment = recruitmentRepository.save(Recruitment.builder()
                .title("title")
                .content("content")
                .volunteeringCategory(VolunteeringCategory.EDUCATION)
                .volunteeringType(volunteeringType)
                .volunteerType(VolunteerType.ADULT)
                .maxParticipationNum(9999)
                .currentVolunteerNum(0)
                .isIssued(true)
                .organizationName("unicef")
                .address(address)
                .coordinate(coordinate)
                .timetable(timetable)
                .viewCount(0)
                .likeCount(0)
                .isPublished(true)
                .isDeleted(IsDeleted.N)
                .writer(writer)
                .build());
        recruitment.setRepeatPeriods(repeatPeriods);
        return recruitment;
    }

    private User createAndSaveUser(String nickName, String basicImagePath) {
        return userRepository.save(
                new User("test", "test", nickName, "test@email.com", Gender.M, LocalDate.now(),
                        basicImagePath, true, true, true, Role.USER, "kakao", "1234", null)
        );
    }

    private void createAndSaveUploadImage(RealWorkCode code, Long no, String imagePath) {
        Storage storage = new Storage(imagePath, "test", "test", "png");
        Image image = new Image(code, no);
        image.setStorage(storage);

        imageRepository.save(image);
    }

}