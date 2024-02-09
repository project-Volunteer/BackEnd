package project.volunteer.domain.recruitment.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import project.volunteer.domain.image.domain.Image;
import project.volunteer.domain.image.domain.Storage;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.recruitment.repository.dto.RecruitmentAndUserDetail;
import project.volunteer.domain.user.domain.Gender;
import project.volunteer.domain.user.domain.Role;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.Address;
import project.volunteer.global.common.component.Coordinate;
import project.volunteer.global.common.component.HourFormat;
import project.volunteer.global.common.component.IsDeleted;
import project.volunteer.global.common.component.RealWorkCode;
import project.volunteer.global.common.component.Timetable;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;
import project.volunteer.support.RepositoryTest;

class RecruitmentRepositoryTest extends RepositoryTest {
    private final Address address = new Address("111", "11", "test", "test");
    private final Coordinate coordinate = new Coordinate(1.2F, 2.2F);
    private final Timetable timetable = new Timetable(LocalDate.now(), LocalDate.now(), HourFormat.AM, LocalTime.now(),
            10);

    @DisplayName("id로 봉사 모집글과 작성자 정보를 함께 조회한다.")
    @Test
    void findRecruitmentAndWriterDetail() {
        //given
        final String nickName = "bonsik";
        final String basicImagePath = "http://...";
        User writer = createAndSaveUser(nickName, basicImagePath);
        Recruitment recruitment = createAndSaveRecruitment(writer, IsDeleted.N);

        //when
        RecruitmentAndUserDetail detail = recruitmentRepository.findRecruitmentAndUserDetailBy(
                recruitment.getRecruitmentNo());

        //then
        assertAll(
                () -> assertThat(detail.getNo()).isEqualTo(recruitment.getRecruitmentNo()),
                () -> assertThat(detail.getRecruitmentImagePath()).isNull(),
                () -> assertThat(detail.getUserNickName()).isEqualTo(nickName),
                () -> assertThat(detail.getUserImagePath()).isEqualTo(basicImagePath)
        );
    }

    @DisplayName("id로 봉사 모집글과 작성자 정보를 조회할 때, 업로드한 이미지 정보도 함께 조회한다.")
    @Test
    void findRecruitmentAndWriterDetailWithUploadImage() {
        //given
        final String nickName = "bonsik";
        final String basicImagePath = "http://...";
        final String userUploadImagePath = "http://s3-user...";
        final String recruitmentUploadImagePath = "http://s3-recruitment...";

        User writer = createAndSaveUser(nickName, basicImagePath);
        Recruitment recruitment = createAndSaveRecruitment(writer, IsDeleted.N);

        createAndSaveUploadImage(RealWorkCode.RECRUITMENT, recruitment.getRecruitmentNo(), recruitmentUploadImagePath);
        createAndSaveUploadImage(RealWorkCode.USER, writer.getUserNo(), userUploadImagePath);

        //when
        RecruitmentAndUserDetail detail = recruitmentRepository.findRecruitmentAndUserDetailBy(
                recruitment.getRecruitmentNo());

        //then
        assertAll(
                () -> assertThat(detail.getNo()).isEqualTo(recruitment.getRecruitmentNo()),
                () -> assertThat(detail.getRecruitmentImagePath()).isEqualTo(recruitmentUploadImagePath),
                () -> assertThat(detail.getUserNickName()).isEqualTo(nickName),
                () -> assertThat(detail.getUserImagePath()).isEqualTo(userUploadImagePath)
        );
    }

    @DisplayName("삭제된 봉사 모집글을 상세 조회할 경우, 예외가 발생한다.")
    @Test
    void findDeletedRecruitmentDetail() {
        //given
        Recruitment recruitment = createAndSaveRecruitment(null, IsDeleted.Y);

        //when & then
        assertThatThrownBy(() -> recruitmentRepository.findRecruitmentAndUserDetailBy(recruitment.getRecruitmentNo()))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.NOT_EXIST_RECRUITMENT.name());
    }

    private Recruitment createAndSaveRecruitment(User writer, IsDeleted isDeleted) {
        return recruitmentRepository.save(Recruitment.builder()
                .title("title")
                .content("content")
                .volunteeringCategory(VolunteeringCategory.EDUCATION)
                .volunteeringType(VolunteeringType.IRREG)
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
                .isDeleted(isDeleted)
                .writer(writer)
                .build());
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