package project.volunteer.domain.sehedule.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.global.common.component.Address;
import project.volunteer.global.common.component.Coordinate;
import project.volunteer.global.common.component.HourFormat;
import project.volunteer.global.common.component.IsDeleted;
import project.volunteer.global.common.component.Timetable;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

class ScheduleTest {
    private final Timetable timetable = new Timetable(LocalDate.now(), LocalDate.now(), HourFormat.AM, LocalTime.now(),
            10);
    private final Address address = new Address("1111", "111", "삼성 아파트", "대구광역시 북구 삼성 아파트");
    private final Coordinate coordinate = new Coordinate(1.2F, 2.2F);
    private final Recruitment recruitment = Recruitment.builder()
            .title("title")
            .content("content")
            .volunteeringCategory(VolunteeringCategory.EDUCATION)
            .volunteerType(VolunteerType.ADULT)
            .volunteeringType(VolunteeringType.IRREG)
            .maxParticipationNum(9999)
            .currentVolunteerNum(0)
            .isIssued(true)
            .organizationName("organization")
            .address(address)
            .coordinate(coordinate)
            .timetable(timetable)
            .viewCount(0)
            .likeCount(0)
            .isPublished(true)
            .isDeleted(IsDeleted.N)
            .build();

    @ParameterizedTest
    @ValueSource(strings = {"가", "구본식의 봉사기관", "unicef", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"})
    @DisplayName("기관이름은 1~50자까지만 가능하다.")
    void checkOrganizationNameSizeWhenCreate(String organizationName) {

        assertThatCode(() -> Schedule.builder()
                .timetable(timetable)
                .content("모집")
                .organizationName(organizationName)
                .address(address)
                .participationNum(10)
                .isDeleted(IsDeleted.N)
                .currentVolunteerNum(0)
                .recruitment(recruitment)
                .build())
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"})
    @DisplayName("기관이름이 1~50자가 아니라면 예외가 발생한다.")
    void throwExceptionWhenCreateWrongOrganizationNameSize(String invalidOrganizationName) {

        assertThatThrownBy(() -> Schedule.builder()
                .timetable(timetable)
                .content("모집")
                .organizationName(invalidOrganizationName)
                .address(address)
                .participationNum(10)
                .isDeleted(IsDeleted.N)
                .currentVolunteerNum(0)
                .recruitment(recruitment)
                .build())
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.INVALID_ORGANIZATION_NAME_SIZE.name());
    }

    @ParameterizedTest
    @ValueSource(strings = {"다들", "조심히", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"})
    @DisplayName("본문은 0~50자까지만 가능하다.")
    void checkContentSizeWhenCreate(String content) {

        assertThatCode(() -> Schedule.builder()
                .timetable(timetable)
                .content(content)
                .organizationName("unicef")
                .address(address)
                .participationNum(10)
                .isDeleted(IsDeleted.N)
                .currentVolunteerNum(0)
                .recruitment(recruitment)
                .build())
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"})
    @DisplayName("본문은 0~50자가 아니라면 예외가 발생한다.")
    void throwExceptionWhenCreateWrongContentSize(String invalidContent) {

        assertThatThrownBy(() -> Schedule.builder()
                .timetable(timetable)
                .content(invalidContent)
                .organizationName("unicef")
                .address(address)
                .participationNum(10)
                .isDeleted(IsDeleted.N)
                .currentVolunteerNum(0)
                .recruitment(recruitment)
                .build())
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.INVALID_CONTENT_SIZE.name());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 9999})
    @DisplayName("참여 인원은 1~9999까지만 가능하다.")
    void checkParticipationNumWhenCreate(int participationNum) {

        assertThatCode(() -> Schedule.builder()
                .timetable(timetable)
                .content("조심히")
                .organizationName("unicef")
                .address(address)
                .participationNum(participationNum)
                .isDeleted(IsDeleted.N)
                .currentVolunteerNum(0)
                .recruitment(recruitment)
                .build())
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 10000})
    @DisplayName("참여 인원이 1~9999가 아닐 경우 예외가 발생한다.")
    void throwExceptionWhenCreateWrongParticipationNum(int invalidParticipationNum) {

        assertThatThrownBy(() -> Schedule.builder()
                .timetable(timetable)
                .content("조심히")
                .organizationName("unicef")
                .address(address)
                .participationNum(invalidParticipationNum)
                .isDeleted(IsDeleted.N)
                .currentVolunteerNum(0)
                .recruitment(recruitment)
                .build())
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.INVALID_PARTICIPATION_NUM.name());
    }

    @ParameterizedTest
    @ValueSource(ints = {10, 15})
    @DisplayName("참여 인원 수가 모집글 참여 인원 수보다 많을 경우 예외가 발생한다.")
    void throwExceptionWhenCreateExceedParticipationNumThanRecruitmentParticipationNum(int invalidParticipationNum) {
        // given
        final Recruitment recruitment = createRecruitment(9);

        // when & then
        assertThatThrownBy(() -> Schedule.builder()
                .timetable(timetable)
                .content("조심히")
                .organizationName("unicef")
                .address(address)
                .participationNum(invalidParticipationNum)
                .isDeleted(IsDeleted.N)
                .currentVolunteerNum(0)
                .recruitment(recruitment)
                .build())
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.EXCEED_PARTICIPATION_NUM_THAN_RECRUITMENT_PARTICIPATION_NUM.name());
    }

    @Test
    @DisplayName("일정 정보를 수정할 수 있다.")
    void changeSchedule() {
        // given
        final Schedule schedule = Schedule.builder()
                .timetable(timetable)
                .content("test")
                .organizationName("test")
                .address(address)
                .participationNum(10)
                .isDeleted(IsDeleted.N)
                .currentVolunteerNum(9)
                .recruitment(recruitment)
                .build();

        final Timetable expectTimetable = new Timetable(LocalDate.now(), LocalDate.now(), HourFormat.PM,
                LocalTime.now(), 20);
        final Address expectAddress = new Address("222", "222", "change", "change");
        final int expectParticipationNum = 100;
        final String expectContent = "change";
        final String expectOrganizationName = "change";

        // when
        schedule.change(recruitment, expectTimetable, expectContent, expectOrganizationName, expectAddress,
                expectParticipationNum);

        // then
        assertThat(schedule).extracting("content", "organizationName", "volunteerNum")
                .containsExactlyInAnyOrder(expectContent, expectOrganizationName, expectParticipationNum);
    }

    @ParameterizedTest
    @ValueSource(ints = {5, 7})
    @DisplayName("수정할 참여 인원 수가 현재 참여 인원 수보다 적을 경우 예외가 발생한다.")
    void throwExceptionWhenChangeExceedParticipationNumThanCurrentParticipationNum(int invalidParticipationNum) {
        // given
        final Schedule schedule = createSchedule(10, 8);

        // when & then
        assertThatThrownBy(
                () -> schedule.change(recruitment, timetable, "test", "test", address, invalidParticipationNum))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.LESS_PARTICIPATION_NUM_THAN_CURRENT_PARTICIPANT.name());
    }

    @ParameterizedTest
    @ValueSource(strings = {"2024-01-25", "2024-01-26", "2024-02-10"})
    @DisplayName("현재 날짜가 일정 시작일 이후일 경우 예외가 발생한다.")
    void throwExceptionWhenCurrentDateAfterStartDate(String invalidDateStr) {
        //given
        final LocalDate invalidDate = toLocalDate(invalidDateStr);
        final LocalDate currentDate = LocalDate.of(2024, 2, 11);

        final Schedule schedule = new Schedule(
                new Timetable(invalidDate, invalidDate, HourFormat.AM, LocalTime.now(), 10),
                "test", "test", address, 10, IsDeleted.N, 8, recruitment);

        //when & then
        assertThatThrownBy(
                () -> schedule.checkDoneDate(currentDate))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.EXPIRED_PERIOD_SCHEDULE.name());
    }

    private LocalDate toLocalDate(String date) {
        return LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    private Recruitment createRecruitment(int participationNum) {
        Coordinate coordinate = new Coordinate(1.2F, 2.2F);
        return Recruitment.builder()
                .title("test")
                .content("content")
                .volunteeringCategory(VolunteeringCategory.EDUCATION)
                .volunteerType(VolunteerType.ADULT)
                .volunteeringType(VolunteeringType.IRREG)
                .maxParticipationNum(participationNum)
                .currentVolunteerNum(0)
                .isIssued(true)
                .organizationName("test")
                .address(address)
                .coordinate(coordinate)
                .timetable(timetable)
                .isPublished(true)
                .build();
    }

    private Schedule createSchedule(int participationNum, int currentParticipationNum) {
        return Schedule.builder()
                .timetable(timetable)
                .content("test")
                .organizationName("test")
                .address(address)
                .participationNum(participationNum)
                .isDeleted(IsDeleted.N)
                .currentVolunteerNum(currentParticipationNum)
                .recruitment(recruitment)
                .build();
    }
}