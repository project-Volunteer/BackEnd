package project.volunteer.domain.recruitment.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import project.volunteer.domain.image.domain.Image;
import project.volunteer.domain.image.domain.Storage;
import project.volunteer.domain.participation.domain.Participant;
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
        participantRepository.save(new Participant(recruitment, user1, ParticipantState.JOIN_REQUEST));
        participantRepository.save(new Participant(recruitment, user2, ParticipantState.JOIN_APPROVAL));

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
                () -> assertThat(result.getRepeatPeriod().getDays()).hasSize(2)
                        .containsExactlyInAnyOrder(Day.MON.getId(), Day.TUES.getId()),
                () -> assertThat(result.getApprovalParticipant()).hasSize(1)
                        .extracting("userNo", "nickName", "imageUrl")
                        .containsExactlyInAnyOrder(tuple(user2.getUserNo(), user2.getNickName(), userUploadImagePath2)),
                () -> assertThat(result.getRequiredParticipant()).hasSize(1)
                        .extracting("userNo", "nickName", "imageUrl")
                        .containsExactlyInAnyOrder(tuple(user1.getUserNo(), user1.getNickName(), userUploadImagePath1))
        );
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