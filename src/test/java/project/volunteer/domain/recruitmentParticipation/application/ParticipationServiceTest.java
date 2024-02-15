package project.volunteer.domain.recruitmentParticipation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.recruitmentParticipation.domain.RecruitmentParticipation;
import project.volunteer.domain.user.domain.Gender;
import project.volunteer.domain.user.domain.Role;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.Address;
import project.volunteer.global.common.component.Coordinate;
import project.volunteer.global.common.component.HourFormat;
import project.volunteer.global.common.component.IsDeleted;
import project.volunteer.global.common.component.ParticipantState;
import project.volunteer.global.common.component.Timetable;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;
import project.volunteer.support.ServiceTest;

class ParticipationServiceTest extends ServiceTest {
    private final Timetable timetable = new Timetable(LocalDate.now(), LocalDate.now(), HourFormat.AM, LocalTime.now(),
            10);
    private final Address address = new Address("1111", "111", "삼성 아파트", "대구광역시 북구 삼성 아파트");
    private final Coordinate coordinate = new Coordinate(1.2F, 2.2F);
    private final User user = new User("test", "test", "test", "test@email.com", Gender.M, LocalDate.now(),
            "http://...", true, true, true, Role.USER, "kakao", "1234", null);

    @BeforeEach
    void setUp() {
        userRepository.save(user);
    }

    @DisplayName("봉사 모집글 첫 가입 신청에 성공한다.")
    @Test
    void join() {
        //given
        final Recruitment recruitment = createAndSaveRecruitment(100, 10);

        //when
        Long recruitmentParticipationNo = recruitmentParticipationService.join(user, recruitment);

        //then
        RecruitmentParticipation recruitmentParticipation = findRecruitmentParticipation(recruitmentParticipationNo);
        assertThat(recruitmentParticipation.getState()).isEqualByComparingTo(ParticipantState.JOIN_REQUEST);
    }

    @DisplayName("중복 가입 신청일 경우 예외를 발생시킨다.")
    @Test
    void duplicationJoin() {
        //given
        final Recruitment recruitment = createAndSaveRecruitment(100, 10);
        recruitmentParticipationRepository.save(
                new RecruitmentParticipation(recruitment, user, ParticipantState.JOIN_APPROVAL));

        //when & then
        assertThatThrownBy(() -> recruitmentParticipationService.join(user, recruitment))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.DUPLICATE_RECRUITMENT_PARTICIPATION.name());
    }

    @DisplayName("봉사 모집글 참여 인원이 가득찬 경우 예외를 발생시킨다.")
    @Test
    void joinFullRecruitment() {
        //given
        final Recruitment recruitment = createAndSaveRecruitment(100, 100);

        //when & then
        assertThatThrownBy(() -> recruitmentParticipationService.join(user, recruitment))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.INSUFFICIENT_CAPACITY.name());
    }

    @DisplayName("봉사 모집글 재 가입 신청에 성공한다.")
    @Test
    void reJoin() {
        //given
        final Recruitment recruitment = createAndSaveRecruitment(100, 10);
        recruitmentParticipationRepository.save(new RecruitmentParticipation(recruitment, user, ParticipantState.JOIN_CANCEL));

        //when
        Long recruitmentParticipationNo = recruitmentParticipationService.join(user, recruitment);

        //then
        RecruitmentParticipation recruitmentParticipation = findRecruitmentParticipation(recruitmentParticipationNo);
        assertThat(recruitmentParticipation.getState()).isEqualByComparingTo(ParticipantState.JOIN_REQUEST);
    }

    private Recruitment createAndSaveRecruitment(int maxParticipationNum, int currentParticipationNum) {
        return recruitmentRepository.save(Recruitment.builder()
                .title("title")
                .content("content")
                .volunteeringCategory(VolunteeringCategory.EDUCATION)
                .volunteerType(VolunteerType.ADULT)
                .volunteeringType(VolunteeringType.IRREG)
                .maxParticipationNum(maxParticipationNum)
                .currentVolunteerNum(currentParticipationNum)
                .isIssued(true)
                .organizationName("organization")
                .address(address)
                .coordinate(coordinate)
                .timetable(timetable)
                .viewCount(0)
                .likeCount(0)
                .isPublished(true)
                .isDeleted(IsDeleted.N)
                .build());
    }

    private RecruitmentParticipation findRecruitmentParticipation(Long id) {
        return recruitmentParticipationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("봉사 모집글 신청 정보가 존재하지 않습니다."));
    }

}