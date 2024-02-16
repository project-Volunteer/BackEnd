package project.volunteer.domain.recruitment.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import project.volunteer.global.common.component.Address;
import project.volunteer.global.common.component.Coordinate;
import project.volunteer.global.common.component.HourFormat;
import project.volunteer.global.common.component.IsDeleted;
import project.volunteer.global.common.component.Timetable;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

class RecruitmentTest {
    private final Timetable timetable = new Timetable(LocalDate.now(), LocalDate.now(), HourFormat.AM, LocalTime.now(),
            10);
    private final Address address = new Address("1111", "111", "삼성 아파트", "대구광역시 북구 삼성 아파트");
    private final Coordinate coordinate = new Coordinate(1.2F, 2.2F);

    @ParameterizedTest
    @ValueSource(strings = {"가", "구본식의 봉사기관", "unicef", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"})
    @DisplayName("기관이름은 1~50자까지만 가능하다.")
    void checkOrganizationNameSizeWhenCreate(String organizationName) {
        assertThatCode(() -> Recruitment.builder()
                .title("title")
                .content("content")
                .volunteeringCategory(VolunteeringCategory.EDUCATION)
                .volunteerType(VolunteerType.ADULT)
                .volunteeringType(VolunteeringType.IRREG)
                .maxParticipationNum(999)
                .currentVolunteerNum(0)
                .isIssued(true)
                .organizationName(organizationName)
                .address(address)
                .coordinate(coordinate)
                .timetable(timetable)
                .viewCount(0)
                .likeCount(0)
                .isPublished(true)
                .isDeleted(IsDeleted.N)
                .build())
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"})
    @DisplayName("기관이름이 1~50자가 아니라면 예외가 발생한다.")
    void throwExceptionWhenCreateWrongOrganizationNameSize(String invalidOrganizationName) {
        assertThatThrownBy(() -> Recruitment.builder()
                .title("title")
                .content("content")
                .volunteeringCategory(VolunteeringCategory.EDUCATION)
                .volunteerType(VolunteerType.ADULT)
                .volunteeringType(VolunteeringType.IRREG)
                .maxParticipationNum(999)
                .currentVolunteerNum(0)
                .isIssued(true)
                .organizationName(invalidOrganizationName)
                .address(address)
                .coordinate(coordinate)
                .timetable(timetable)
                .viewCount(0)
                .likeCount(0)
                .isPublished(true)
                .isDeleted(IsDeleted.N)
                .build())
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.INVALID_ORGANIZATION_NAME_SIZE.name());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 9999})
    @DisplayName("참여 인원은 1~9999까지만 가능하다.")
    void checkParticipationNumWhenCreate(int maxParticipationNum) {
        assertThatCode(() -> Recruitment.builder()
                .title("title")
                .content("content")
                .volunteeringCategory(VolunteeringCategory.EDUCATION)
                .volunteerType(VolunteerType.ADULT)
                .volunteeringType(VolunteeringType.IRREG)
                .maxParticipationNum(maxParticipationNum)
                .currentVolunteerNum(0)
                .isIssued(true)
                .organizationName("orgiacation")
                .address(address)
                .coordinate(coordinate)
                .timetable(timetable)
                .viewCount(0)
                .likeCount(0)
                .isPublished(true)
                .isDeleted(IsDeleted.N)
                .build())
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 10000})
    @DisplayName("참여 인원이 1~9999가 아닐 경우 예외가 발생한다.")
    void throwExceptionWhenCreateWrongParticipationNum(int invalidParticipationNum) {
        assertThatThrownBy(() -> Recruitment.builder()
                .title("title")
                .content("content")
                .volunteeringCategory(VolunteeringCategory.EDUCATION)
                .volunteerType(VolunteerType.ADULT)
                .volunteeringType(VolunteeringType.IRREG)
                .maxParticipationNum(invalidParticipationNum)
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
                .build())
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.INVALID_PARTICIPATION_NUM.name());
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "",
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"})
    @DisplayName("제목이 1~255자가 아니라면 예외가 발생한다.")
    void throwExceptionWhenCreateWrongTitle(String invalidTitle) {
        assertThatThrownBy(() -> Recruitment.builder()
                .title(invalidTitle)
                .content("content")
                .volunteeringCategory(VolunteeringCategory.EDUCATION)
                .volunteerType(VolunteerType.ADULT)
                .volunteeringType(VolunteeringType.IRREG)
                .maxParticipationNum(999)
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
                .build())
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.INVALID_TITLE_SIZE.name());
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "",
            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"})
    @DisplayName("본문이 1~255자가 아니라면 예외가 발생한다.")
    void throwExceptionWhenCreateWrongContent(String invalidContent) {
        assertThatThrownBy(() -> Recruitment.builder()
                .title("title")
                .content(invalidContent)
                .volunteeringCategory(VolunteeringCategory.EDUCATION)
                .volunteerType(VolunteerType.ADULT)
                .volunteeringType(VolunteeringType.IRREG)
                .maxParticipationNum(999)
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
                .build())
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.INVALID_CONTENT_SIZE.name());
    }

    @ParameterizedTest
    @ValueSource(strings = {"2024-01-10", "2024-01-15"})
    @DisplayName("현재 날짜가 모집글 마감일 이후일 경우 예외가 발생한다.")
    void throwExceptionWhenAfterEndDate(String invalidEndDateStr) {
        //given
        final LocalDate invalidEndDate = toLocalDate(invalidEndDateStr);
        final LocalDate startDate = invalidEndDate.minusDays(2);
        final LocalDate now = LocalDate.of(2024, 1, 16);

        final Recruitment recruitment = Recruitment.builder()
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
                .timetable(new Timetable(startDate, invalidEndDate, HourFormat.AM, LocalTime.now(), 10))
                .viewCount(0)
                .likeCount(0)
                .isPublished(true)
                .isDeleted(IsDeleted.N)
                .build();

        //when & then
        assertThatThrownBy(() -> recruitment.checkDoneDate(now))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.EXPIRED_PERIOD_RECRUITMENT.name());
    }

    @ParameterizedTest
    @ValueSource(ints = {3, 4, 5, 6})
    @DisplayName("참여를 승인할 인원 수가 남은 참여 가능 수보다 많을 경우 예외가 발생한다.")
    void throwExceptionWhenExceedParticipationNum(int invalidAddParticipationNum) {
        //given
        final Recruitment recruitment = Recruitment.builder()
                .title("title")
                .content("content")
                .volunteeringCategory(VolunteeringCategory.EDUCATION)
                .volunteerType(VolunteerType.ADULT)
                .volunteeringType(VolunteeringType.IRREG)
                .maxParticipationNum(10)
                .currentVolunteerNum(8)
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

        //when & then
        assertThatThrownBy(() -> recruitment.increaseParticipationNum(invalidAddParticipationNum))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.INSUFFICIENT_APPROVAL_CAPACITY.name());
    }

    private LocalDate toLocalDate(String date) {
        return LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

}