package project.volunteer.domain.recruitment.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import project.volunteer.domain.image.domain.Image;
import project.volunteer.domain.image.domain.Storage;
import project.volunteer.domain.recruitment.application.dto.query.list.RecruitmentList;
import project.volunteer.domain.recruitment.application.dto.query.list.RecruitmentSearchCond;
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
                        recruitment.getRecruitmentNo())
                .orElseThrow(() -> new IllegalArgumentException("모집글 정보가 존재하지 않습니다."));

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
                        recruitment.getRecruitmentNo())
                .orElseThrow(() -> new IllegalArgumentException("모집글 정보가 존재하지 않습니다."));

        //then
        assertAll(
                () -> assertThat(detail.getNo()).isEqualTo(recruitment.getRecruitmentNo()),
                () -> assertThat(detail.getRecruitmentImagePath()).isEqualTo(recruitmentUploadImagePath),
                () -> assertThat(detail.getUserNickName()).isEqualTo(nickName),
                () -> assertThat(detail.getUserImagePath()).isEqualTo(userUploadImagePath)
        );
    }

    @DisplayName("필터링 조건으로 봉사 모집글 리스트를 조회한다.")
    @Test
    void findRecruitmentListBySearchCond() {
        //given
        final String imagePath1 = "http://recruitment1";
        final String imagePath2 = "http://recruitment2";
        final String imagePath3 = "http://recruitment3";

        User writer = userRepository.save(
                new User("test", "test", "test", "test@email.com", Gender.M, LocalDate.now(),
                        "http://", true, true, true, Role.USER, "kakao", "1234", null));
        Recruitment recruitment1 = recruitmentRepository.save(
                new Recruitment("title1", "content1", VolunteeringCategory.ADMINSTRATION_ASSISTANCE,
                        VolunteeringType.IRREG, VolunteerType.TEENAGER, 9999, 0, true, "unicef", address, coordinate,
                        timetable, 0, 0, true, IsDeleted.N, writer));
        Recruitment recruitment2 = recruitmentRepository.save(
                new Recruitment("title2", "content2", VolunteeringCategory.EDUCATION,
                        VolunteeringType.IRREG, VolunteerType.ADULT, 9999, 0, true, "unicef", address, coordinate,
                        timetable, 0, 0, true, IsDeleted.N, writer));
        Recruitment recruitment3 = recruitmentRepository.save(
                new Recruitment("title3", "content3", VolunteeringCategory.DISASTER,
                        VolunteeringType.IRREG, VolunteerType.TEENAGER, 9999, 0, true, "unicef", address, coordinate,
                        timetable, 0, 0, true, IsDeleted.N, writer));

        createAndSaveUploadImage(RealWorkCode.RECRUITMENT, recruitment1.getRecruitmentNo(), imagePath1);
        createAndSaveUploadImage(RealWorkCode.RECRUITMENT, recruitment2.getRecruitmentNo(), imagePath2);
        createAndSaveUploadImage(RealWorkCode.RECRUITMENT, recruitment3.getRecruitmentNo(), imagePath3);

        final RecruitmentSearchCond searchCond = new RecruitmentSearchCond(
                List.of(VolunteeringCategory.ADMINSTRATION_ASSISTANCE, VolunteeringCategory.DISASTER), null, null, null,
                VolunteerType.TEENAGER, null);

        //when
        Slice<RecruitmentList> actual = recruitmentRepository.findRecruitmentListBy(PageRequest.of(0, 10), searchCond);

        //then
        assertAll(
                () -> assertThat(actual).hasSize(2),
                () -> assertThat(actual.isLast()).isTrue(),
                () -> assertThat(actual)
                        .extracting("no", "picture.isStaticImage", "picture.uploadImage")
                        .containsExactly(
                                tuple(recruitment3.getRecruitmentNo(), false, imagePath3),
                                tuple(recruitment1.getRecruitmentNo(), false, imagePath1))
        );
    }

    @DisplayName("필터링 조건으로 두번째 페이지 봉사 모집글 리스트를 조회한다.")
    @Test
    void findSecondPageRecruitmentListBySearchCond() {
        //given
        final String imagePath1 = "http://recruitment1";
        final String imagePath2 = "http://recruitment2";
        final String imagePath3 = "http://recruitment3";
        final String imagePath4 = "http://recruitment4";
        final String imagePath5 = "http://recruitment5";

        User writer = userRepository.save(
                new User("test", "test", "test", "test@email.com", Gender.M, LocalDate.now(),
                        "http://", true, true, true, Role.USER, "kakao", "1234", null));
        Recruitment recruitment1 = recruitmentRepository.save(
                new Recruitment("title1", "content1", VolunteeringCategory.ADMINSTRATION_ASSISTANCE,
                        VolunteeringType.IRREG, VolunteerType.TEENAGER, 9999, 0, true, "unicef", address, coordinate,
                        timetable, 0, 0, true, IsDeleted.N, writer));
        Recruitment recruitment2 = recruitmentRepository.save(
                new Recruitment("title2", "content2", VolunteeringCategory.EDUCATION,
                        VolunteeringType.IRREG, VolunteerType.ADULT, 9999, 0, true, "unicef", address, coordinate,
                        timetable, 0, 0, true, IsDeleted.N, writer));
        Recruitment recruitment3 = recruitmentRepository.save(
                new Recruitment("title3", "content3", VolunteeringCategory.ETC,
                        VolunteeringType.IRREG, VolunteerType.ALL, 9999, 0, true, "unicef", address, coordinate,
                        timetable, 0, 0, true, IsDeleted.N, writer));
        Recruitment recruitment4 = recruitmentRepository.save(
                new Recruitment("title4", "content4", VolunteeringCategory.FOREIGN_COUNTRY,
                        VolunteeringType.IRREG, VolunteerType.ALL, 9999, 0, true, "unicef", address, coordinate,
                        timetable, 0, 0, true, IsDeleted.N, writer));
        Recruitment recruitment5 = recruitmentRepository.save(
                new Recruitment("title5", "content5", VolunteeringCategory.DISASTER,
                        VolunteeringType.IRREG, VolunteerType.ADULT, 9999, 0, true, "unicef", address, coordinate,
                        timetable, 0, 0, true, IsDeleted.N, writer));

        createAndSaveUploadImage(RealWorkCode.RECRUITMENT, recruitment1.getRecruitmentNo(), imagePath1);
        createAndSaveUploadImage(RealWorkCode.RECRUITMENT, recruitment2.getRecruitmentNo(), imagePath2);
        createAndSaveUploadImage(RealWorkCode.RECRUITMENT, recruitment3.getRecruitmentNo(), imagePath3);
        createAndSaveUploadImage(RealWorkCode.RECRUITMENT, recruitment4.getRecruitmentNo(), imagePath4);
        createAndSaveUploadImage(RealWorkCode.RECRUITMENT, recruitment5.getRecruitmentNo(), imagePath5);

        final RecruitmentSearchCond searchCond = new RecruitmentSearchCond(
                null, null, null, VolunteeringType.IRREG, null, true);

        //when
        Slice<RecruitmentList> actual = recruitmentRepository.findRecruitmentListBy(PageRequest.of(1, 2), searchCond);

        //then
        assertAll(
                () -> assertThat(actual).hasSize(2),
                () -> assertThat(actual.isLast()).isFalse(),
                () -> assertThat(actual)
                        .extracting("no", "picture.isStaticImage", "picture.uploadImage")
                        .containsExactly(
                                tuple(recruitment3.getRecruitmentNo(), false, imagePath3),
                                tuple(recruitment2.getRecruitmentNo(), false, imagePath2))
        );
    }

    @DisplayName("title에 키워드가 포함된 봉사 모집글 리스트를 조회한다.")
    @Test
    void findRecruitmentListByKeyWard() {
        //given
        final String keyWord = "bonsik";
        final String title1 = "bonsik-title";
        final String title2 = "title";
        final String title3 = "title-bonsik";

        User writer = userRepository.save(
                new User("test", "test", "test", "test@email.com", Gender.M, LocalDate.now(),
                        "http://", true, true, true, Role.USER, "kakao", "1234", null));
        Recruitment recruitment1 = recruitmentRepository.save(
                new Recruitment(title1, "content1", VolunteeringCategory.ADMINSTRATION_ASSISTANCE,
                        VolunteeringType.IRREG, VolunteerType.TEENAGER, 9999, 0, true, "unicef", address, coordinate,
                        timetable, 0, 0, true, IsDeleted.N, writer));
        Recruitment recruitment2 = recruitmentRepository.save(
                new Recruitment(title2, "content2", VolunteeringCategory.EDUCATION,
                        VolunteeringType.IRREG, VolunteerType.ADULT, 9999, 0, true, "unicef", address, coordinate,
                        timetable, 0, 0, true, IsDeleted.N, writer));
        Recruitment recruitment3 = recruitmentRepository.save(
                new Recruitment(title3, "content3", VolunteeringCategory.DISASTER,
                        VolunteeringType.IRREG, VolunteerType.TEENAGER, 9999, 0, true, "unicef", address, coordinate,
                        timetable, 0, 0, true, IsDeleted.N, writer));

        //when
        Slice<RecruitmentList> actual = recruitmentRepository.findRecruitmentListByTitle(PageRequest.of(0, 5), keyWord);

        //then
        assertAll(
                () -> assertThat(actual).hasSize(2),
                () -> assertThat(actual.isLast()).isTrue(),
                () -> assertThat(actual)
                        .extracting("no", "title")
                        .containsExactly(
                                tuple(recruitment3.getRecruitmentNo(), title3),
                                tuple(recruitment1.getRecruitmentNo(), title1))
        );
    }

    @DisplayName("필터링 조건으로 봉사 모집글 개수를 조회한다.")
    @Test
    void findRecruitmentCountBySearchCond() {
        User writer = userRepository.save(
                new User("test", "test", "test", "test@email.com", Gender.M, LocalDate.now(),
                        "http://", true, true, true, Role.USER, "kakao", "1234", null));
        recruitmentRepository.save(
                new Recruitment("title1", "content1", VolunteeringCategory.ADMINSTRATION_ASSISTANCE,
                        VolunteeringType.IRREG, VolunteerType.TEENAGER, 9999, 0, true, "unicef", address, coordinate,
                        timetable, 0, 0, true, IsDeleted.N, writer));
        recruitmentRepository.save(
                new Recruitment("title2", "content2", VolunteeringCategory.EDUCATION,
                        VolunteeringType.IRREG, VolunteerType.TEENAGER, 9999, 0, false, "unicef", address, coordinate,
                        timetable, 0, 0, true, IsDeleted.N, writer));
        recruitmentRepository.save(
                new Recruitment("title3", "content3", VolunteeringCategory.ADMINSTRATION_ASSISTANCE,
                        VolunteeringType.IRREG, VolunteerType.TEENAGER, 9999, 0, false, "unicef", address, coordinate,
                        timetable, 0, 0, true, IsDeleted.N, writer));
        recruitmentRepository.save(
                new Recruitment("title4", "content4", VolunteeringCategory.FOREIGN_COUNTRY,
                        VolunteeringType.IRREG, VolunteerType.TEENAGER, 9999, 0, false, "unicef", address, coordinate,
                        timetable, 0, 0, true, IsDeleted.N, writer));
        recruitmentRepository.save(
                new Recruitment("title5", "content5", VolunteeringCategory.DISASTER,
                        VolunteeringType.IRREG, VolunteerType.ALL, 9999, 0, true, "unicef", address, coordinate,
                        timetable, 0, 0, true, IsDeleted.N, writer));

        final RecruitmentSearchCond searchCond = new RecruitmentSearchCond(
                List.of(VolunteeringCategory.EDUCATION, VolunteeringCategory.ADMINSTRATION_ASSISTANCE), null, null,
                null, VolunteerType.TEENAGER, false);

        //when
        Long result = recruitmentRepository.findRecruitmentCountBy(searchCond);

        //then
        assertThat(result).isEqualTo(2);
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