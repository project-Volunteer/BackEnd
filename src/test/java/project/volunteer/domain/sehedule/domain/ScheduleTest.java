package project.volunteer.domain.sehedule.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.global.common.component.Address;
import project.volunteer.global.common.component.HourFormat;
import project.volunteer.global.common.component.IsDeleted;
import project.volunteer.global.common.component.Timetable;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

class ScheduleTest {
    private final Timetable timetable = new Timetable(LocalDate.now(), LocalDate.now(), HourFormat.AM, LocalTime.now(),
            10);
    private final Address address = new Address("1111", "111", "삼성 아파트", "대구광역시 북구 삼성 아파트");

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
                .build())
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.INVALID_PARTICIPATION_NUM.name());
    }

    @ParameterizedTest
    @ValueSource(ints = {10, 15})
    @DisplayName("참여 인원 수가 모집글 참여 인원 수보다 많을 경우 예외가 발생한다.")
    void throwExceptionWhenCreateExceedParticipationNumThanRecruitmentParticipationNum(int invalidParticipationNum) {
        // given
        final Recruitment recruitment = Recruitment.builder()
                .participationNum(9)
                .build();

        // when & then
        assertThatThrownBy(
                () -> Schedule.create(recruitment, timetable, "조심히", "unicef", address, invalidParticipationNum))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.EXCEED_PARTICIPATION_NUM_THAN_RECRUITMENT_PARTICIPATION_NUM.name());
    }
}