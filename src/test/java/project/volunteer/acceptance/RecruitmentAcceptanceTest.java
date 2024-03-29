package project.volunteer.acceptance;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.given;
import static project.volunteer.acceptance.AcceptanceFixtures.봉사_게시물_등록;
import static project.volunteer.acceptance.AcceptanceFixtures.봉사_게시물_팀원_가입_승인;
import static project.volunteer.acceptance.AcceptanceFixtures.봉사_게시물_팀원_가입_요청;
import static project.volunteer.acceptance.AcceptanceFixtures.봉사_게시물_팀원_가입_취소;

import java.io.File;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import project.volunteer.domain.recruitmentParticipation.api.dto.request.ParticipantAddRequest;
import project.volunteer.domain.recruitment.api.dto.response.StatusResponse;
import project.volunteer.domain.recruitment.application.dto.query.RecruitmentCountResult;
import project.volunteer.domain.recruitment.application.dto.query.detail.RecruitmentDetailSearchResult;
import project.volunteer.domain.recruitment.application.dto.query.list.RecruitmentListSearchResult;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.recruitment.domain.repeatPeriod.Day;
import project.volunteer.domain.recruitment.domain.repeatPeriod.Period;
import project.volunteer.domain.recruitment.domain.repeatPeriod.Week;
import project.volunteer.global.common.component.HourFormat;
import project.volunteer.global.common.dto.StateResult;

public class RecruitmentAcceptanceTest extends AcceptanceTest {

    @DisplayName("봉사 모집글을 정상적으로 등록한다.")
    @Test
    void saveRecruitment() {
        final File file = new File("src/main/resources/static/test/file.PNG");

        given().log().all()
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                .header(AUTHORIZATION_HEADER, bonsikToken)
                .multiPart("picture.uploadImage", file)
                .formParam("volunteeringCategory", VolunteeringCategory.EDUCATION.getId())
                .formParam("organizationName", "unicef")
                .formParam("isIssued", true)
                .formParam("volunteerType", VolunteerType.ALL.getId())
                .formParam("maxParticipationNum", 100)
                .formParam("startDate", "01-10-2024")
                .formParam("endDate", "03-10-2024")
                .formParam("hourFormat", HourFormat.AM.getId())
                .formParam("startTime", "10:00")
                .formParam("progressTime", 10)
                .formParam("title", "test")
                .formParam("content", "test")
                .formParam("isPublished", true)
                .formParam("address.sido", "11")
                .formParam("address.sigungu", "1111")
                .formParam("address.details", "detail")
                .formParam("address.fullName", "fullName")
                .formParam("address.latitude", 3.2F)
                .formParam("address.longitude", 4.5F)
                .formParam("volunteeringType", VolunteeringType.REG.getId())
                .formParam("period", Period.MONTH.getId())
                .formParam("week", Week.FIRST.getId())
                .formParam("dayOfWeeks", List.of(Day.MON.getId(), Day.TUES.getId()))
                .formParam("picture.isStaticImage", false)
                .when().post("/recruitment")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value())
                .extract();
    }

    @DisplayName("등록한 봉사 모집글을 삭제한다.")
    @Test
    void deleteRecruitment() {
        final Long recruitmentNo = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.EDUCATION, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 100, VolunteeringType.IRREG, "01-01-2024", "02-20-2024", HourFormat.AM, "10:00",
                10,
                Period.NONE, Week.NONE, List.of(), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));

        given().log().all()
                .header(AUTHORIZATION_HEADER, bonsikToken)
                .when().delete("/recruitment/{recruitmentNo}", recruitmentNo)
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();
    }

    @DisplayName("방장이 아니면 봉사 모집글을 삭제할 수 없다.")
    @Test
    void deleteRecruitmentNotOwner() {
        final Long recruitmentNo = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.EDUCATION, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 100, VolunteeringType.IRREG, "01-01-2024", "02-20-2024", HourFormat.AM, "10:00",
                10,
                Period.NONE, Week.NONE, List.of(), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));

        given().log().all()
                .header(AUTHORIZATION_HEADER, soeunToken)
                .when().delete("/recruitment/{recruitmentNo}", recruitmentNo)
                .then().log().all()
                .statusCode(HttpStatus.FORBIDDEN.value())
                .extract();
    }

    @DisplayName("특정 봉사 모집글 정보를 상세 조회한다.")
    @Test
    void findRecruitment() {
        given(clock.instant()).willReturn(Instant.parse("2024-02-02T10:00:00Z"));

        final Long recruitmentNo = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.EDUCATION, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 100, VolunteeringType.REG, "01-01-2024", "02-10-2024", HourFormat.AM, "10:00",
                10,
                Period.WEEK, Week.NONE, List.of(Day.MON, Day.FRI), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));

        Long participantNo1 = 봉사_게시물_팀원_가입_요청(soeunToken, recruitmentNo);
        Long participantNo2 = 봉사_게시물_팀원_가입_요청(changHoeunToken, recruitmentNo);

        봉사_게시물_팀원_가입_승인(bonsikToken, recruitmentNo, new ParticipantAddRequest(List.of(participantNo1)));

        RecruitmentDetailSearchResult response = given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(AUTHORIZATION_HEADER, soeunToken)
                .when().get("/recruitment/{no}", recruitmentNo)
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(RecruitmentDetailSearchResult.class);
        assertAll(
                () -> assertThat(response.getNo()).isEqualTo(recruitmentNo),
                () -> assertThat(response.getRepeatPeriod().getPeriod()).isEqualTo(Period.WEEK.getId()),
                () -> assertThat(response.getRepeatPeriod().getWeek()).isEqualTo(Week.NONE.getId()),
                () -> assertThat(response.getRepeatPeriod().getDayOfWeeks()).hasSize(2)
                        .containsExactlyInAnyOrder(Day.MON.getId(), Day.FRI.getId()),
                () -> assertThat(response.getRequiredParticipant()).hasSize(1)
                        .extracting("recruitmentParticipationNo", "nickName")
                        .containsExactlyInAnyOrder(tuple(participantNo2, "changHoeun")),
                () -> assertThat(response.getApprovedParticipant()).hasSize(1)
                        .extracting("recruitmentParticipationNo", "nickName")
                        .containsExactlyInAnyOrder(tuple(participantNo1, "soeun"))
        );
    }

    @DisplayName("001, 002 카테고리에 속하는 봉사 모집글 목록을 필터링 한다.")
    @Test
    void findRecruitmentFilterVolunteerCategory() {
        final Long recruitmentNo1 = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.ADMINSTRATION_ASSISTANCE, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F,
                true,
                VolunteerType.ADULT, 100, VolunteeringType.REG, "01-01-2024", "02-10-2024", HourFormat.AM, "10:00",
                10,
                Period.WEEK, Week.NONE, List.of(Day.MON, Day.FRI), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));
        final Long recruitmentNo2 = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.EDUCATION, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 100, VolunteeringType.REG, "01-01-2024", "02-10-2024", HourFormat.AM, "10:00",
                10,
                Period.WEEK, Week.NONE, List.of(Day.MON, Day.FRI), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));
        final Long recruitmentNo3 = 봉사_게시물_등록(soeunToken,
                VolunteeringCategory.CULTURAL_EVENT, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 100, VolunteeringType.REG, "01-01-2024", "02-10-2024", HourFormat.AM, "10:00",
                10,
                Period.WEEK, Week.NONE, List.of(Day.MON, Day.FRI), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));

        RecruitmentListSearchResult response = given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(AUTHORIZATION_HEADER, changHoeunToken)
                .queryParam("page", 0)
                .queryParam("volunteering_category", List.of(VolunteeringCategory.ADMINSTRATION_ASSISTANCE.getId(),
                        VolunteeringCategory.CULTURAL_EVENT.getId()))
                .when().get("/recruitment")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(RecruitmentListSearchResult.class);
        assertAll(
                () -> assertThat(response.getIsLast()).isTrue(),
                () -> assertThat(response.getRecruitmentList()).hasSize(2)
                        .extracting("no")
                        .containsExactly(recruitmentNo3, recruitmentNo1)
        );
    }

    @DisplayName("정기 봉사 모집글 목록 중 2번째 페이지를 조회한다.")
    @Test
    void findRecruitmentFilterPage() {
        final Long recruitmentNo1 = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.ADMINSTRATION_ASSISTANCE, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F,
                true,
                VolunteerType.ADULT, 100, VolunteeringType.REG, "01-01-2024", "02-10-2024", HourFormat.AM, "10:00",
                10,
                Period.WEEK, Week.NONE, List.of(Day.MON, Day.FRI), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));
        final Long recruitmentNo2 = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.EDUCATION, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 100, VolunteeringType.REG, "01-01-2024", "02-10-2024", HourFormat.AM, "10:00",
                10,
                Period.WEEK, Week.NONE, List.of(Day.MON, Day.FRI), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));
        final Long recruitmentNo3 = 봉사_게시물_등록(soeunToken,
                VolunteeringCategory.CULTURAL_EVENT, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 100, VolunteeringType.REG, "01-01-2024", "02-10-2024", HourFormat.AM, "10:00",
                10,
                Period.WEEK, Week.NONE, List.of(Day.MON, Day.FRI), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));
        final Long recruitmentNo4 = 봉사_게시물_등록(soeunToken,
                VolunteeringCategory.CULTURAL_EVENT, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 100, VolunteeringType.REG, "01-01-2024", "02-10-2024", HourFormat.AM, "10:00",
                10,
                Period.WEEK, Week.NONE, List.of(Day.MON, Day.FRI), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));
        final Long recruitmentNo5 = 봉사_게시물_등록(soeunToken,
                VolunteeringCategory.CULTURAL_EVENT, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 100, VolunteeringType.REG, "01-01-2024", "02-10-2024", HourFormat.AM, "10:00",
                10,
                Period.WEEK, Week.NONE, List.of(Day.MON, Day.FRI), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));
        final Long recruitmentNo6 = 봉사_게시물_등록(soeunToken,
                VolunteeringCategory.CULTURAL_EVENT, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 100, VolunteeringType.REG, "01-01-2024", "02-10-2024", HourFormat.AM, "10:00",
                10,
                Period.WEEK, Week.NONE, List.of(Day.MON, Day.FRI), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));
        final Long recruitmentNo7 = 봉사_게시물_등록(soeunToken,
                VolunteeringCategory.CULTURAL_EVENT, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 100, VolunteeringType.REG, "01-01-2024", "02-10-2024", HourFormat.AM, "10:00",
                10,
                Period.WEEK, Week.NONE, List.of(Day.MON, Day.FRI), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));

        RecruitmentListSearchResult response = given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(AUTHORIZATION_HEADER, changHoeunToken)
                .queryParam("page", 1)
                .queryParam("volunteering_type", VolunteeringType.REG.getId())
                .when().get("/recruitment")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(RecruitmentListSearchResult.class);
        assertAll(
                () -> assertThat(response.getIsLast()).isTrue(),
                () -> assertThat(response.getRecruitmentList()).hasSize(1)
                        .extracting("no")
                        .containsExactly(recruitmentNo1)
        );
    }

    @DisplayName("참여 가능한 봉사 모집글이면 staus는 AVAILABLE로 나온다.")
    @Test
    void getAvailableState() {
        given(clock.instant()).willReturn(Instant.parse("2024-02-02T10:00:00Z"));

        final Long recruitmentNo = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.ADMINSTRATION_ASSISTANCE, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F,
                true,
                VolunteerType.ADULT, 100, VolunteeringType.REG, "01-01-2024", "02-10-2024", HourFormat.AM, "10:00",
                10,
                Period.WEEK, Week.NONE, List.of(Day.MON, Day.FRI), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));

        StatusResponse response = given().log().all()
                .header(AUTHORIZATION_HEADER, soeunToken)
                .when().get("/recruitment/{no}/status", recruitmentNo)
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(StatusResponse.class);
        assertThat(response.getStatus()).isEqualTo(StateResult.AVAILABLE.getId());
    }

    @DisplayName("인원이 가득찬 봉사 모집글이면 staus는 FULL로 나온다.")
    @Test
    void getFullState() {
        given(clock.instant()).willReturn(Instant.parse("2024-02-02T10:00:00Z"));

        final Long recruitmentNo = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.ADMINSTRATION_ASSISTANCE, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F,
                true,
                VolunteerType.ADULT, 1, VolunteeringType.REG, "01-01-2024", "02-10-2024", HourFormat.AM, "10:00",
                10,
                Period.WEEK, Week.NONE, List.of(Day.MON, Day.FRI), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));

        Long participationNo1 = 봉사_게시물_팀원_가입_요청(soeunToken, recruitmentNo);

        봉사_게시물_팀원_가입_승인(bonsikToken, recruitmentNo, new ParticipantAddRequest(List.of(participationNo1)));

        StatusResponse response = given().log().all()
                .header(AUTHORIZATION_HEADER, changHoeunToken)
                .when().get("/recruitment/{no}/status", recruitmentNo)
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(StatusResponse.class);
        assertThat(response.getStatus()).isEqualTo(StateResult.FULL.getId());
    }

    @DisplayName("신청이 가능하고 참여 신청 취소 이력을 가진 봉사 모집글이면 status는 AVAILABLE로 나온다.")
    @Test
    void getAvailableStateCancelledRecruitment() {
        given(clock.instant()).willReturn(Instant.parse("2024-02-02T10:00:00Z"));

        final Long recruitmentNo = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.ADMINSTRATION_ASSISTANCE, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F,
                true,
                VolunteerType.ADULT, 5, VolunteeringType.REG, "01-01-2024", "02-10-2024", HourFormat.AM, "10:00",
                10,
                Period.WEEK, Week.NONE, List.of(Day.MON, Day.FRI), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));

        봉사_게시물_팀원_가입_요청(soeunToken, recruitmentNo);

        봉사_게시물_팀원_가입_취소(soeunToken, recruitmentNo);

        StatusResponse response = given().log().all()
                .header(AUTHORIZATION_HEADER, changHoeunToken)
                .when().get("/recruitment/{no}/status", recruitmentNo)
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(StatusResponse.class);
        assertThat(response.getStatus()).isEqualTo(StateResult.AVAILABLE.getId());
    }

    @DisplayName("마감된 봉사 모집글이면 status는 DONE이 나온다.")
    @Test
    void getDoneState() {
        given(clock.instant()).willReturn(Instant.parse("2024-02-11T10:00:00Z"));

        final Long recruitmentNo = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.ADMINSTRATION_ASSISTANCE, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F,
                true,
                VolunteerType.ADULT, 5, VolunteeringType.REG, "01-01-2024", "02-10-2024", HourFormat.AM, "10:00",
                10,
                Period.WEEK, Week.NONE, List.of(Day.MON, Day.FRI), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));

        StatusResponse response = given().log().all()
                .header(AUTHORIZATION_HEADER, changHoeunToken)
                .when().get("/recruitment/{no}/status", recruitmentNo)
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(StatusResponse.class);
        assertThat(response.getStatus()).isEqualTo(StateResult.DONE.getId());
    }

    @DisplayName("특정 keyward가 title에 포함된 봉사 모집글 목록을 필터링한다.")
    @Test
    void findRecruitmentFilterKeyWard() {
        final Long recruitmentNo1 = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.ADMINSTRATION_ASSISTANCE, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F,
                true,
                VolunteerType.ADULT, 100, VolunteeringType.REG, "01-01-2024", "02-10-2024", HourFormat.AM, "10:00",
                10,
                Period.WEEK, Week.NONE, List.of(Day.MON, Day.FRI), "unicef 정기 봉사 모집", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));
        final Long recruitmentNo2 = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.EDUCATION, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 100, VolunteeringType.REG, "01-01-2024", "02-10-2024", HourFormat.AM, "10:00",
                10,
                Period.WEEK, Week.NONE, List.of(Day.MON, Day.FRI), "매일 함께하는 unicef", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));
        final Long recruitmentNo3 = 봉사_게시물_등록(soeunToken,
                VolunteeringCategory.CULTURAL_EVENT, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 100, VolunteeringType.REG, "01-01-2024", "02-10-2024", HourFormat.AM, "10:00",
                10,
                Period.WEEK, Week.NONE, List.of(Day.MON, Day.FRI), "환영합니다.", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));

        RecruitmentListSearchResult response = given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(AUTHORIZATION_HEADER, changHoeunToken)
                .queryParam("page", 0)
                .queryParam("keyword", "unicef")
                .when().get("/recruitment/keyward")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(RecruitmentListSearchResult.class);
        assertAll(
                () -> assertThat(response.getIsLast()).isTrue(),
                () -> assertThat(response.getRecruitmentList()).hasSize(2)
                        .extracting("no")
                        .containsExactly(recruitmentNo2, recruitmentNo1)
        );
    }

    @DisplayName("003 카테고리에 속하면서 어른만 참여할 수 있는 봉사 모집글 목록 개수를 조회힌다.")
    @Test
    void findRecruitmentCountFilterVolunteerTypeAndCategory() {
        final Long recruitmentNo1 = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.RESIDENTIAL_ENV, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F,
                true,
                VolunteerType.ADULT, 100, VolunteeringType.REG, "01-01-2024", "02-10-2024", HourFormat.AM, "10:00",
                10,
                Period.WEEK, Week.NONE, List.of(Day.MON, Day.FRI), "unicef 정기 봉사 모집", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));
        final Long recruitmentNo2 = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.EDUCATION, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 100, VolunteeringType.REG, "01-01-2024", "02-10-2024", HourFormat.AM, "10:00",
                10,
                Period.WEEK, Week.NONE, List.of(Day.MON, Day.FRI), "매일 함께하는 unicef", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));
        final Long recruitmentNo3 = 봉사_게시물_등록(soeunToken,
                VolunteeringCategory.RESIDENTIAL_ENV, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 100, VolunteeringType.REG, "01-01-2024", "02-10-2024", HourFormat.AM, "10:00",
                10,
                Period.WEEK, Week.NONE, List.of(Day.MON, Day.FRI), "환영합니다.", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));
        final Long recruitmentNo4 = 봉사_게시물_등록(soeunToken,
                VolunteeringCategory.RESIDENTIAL_ENV, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.TEENAGER, 100, VolunteeringType.REG, "01-01-2024", "02-10-2024", HourFormat.AM, "10:00",
                10,
                Period.WEEK, Week.NONE, List.of(Day.MON, Day.FRI), "환영합니다.", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));

        RecruitmentCountResult response = given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(AUTHORIZATION_HEADER, changHoeunToken)
                .queryParam("page", 0)
                .queryParam("volunteering_category", List.of(VolunteeringCategory.RESIDENTIAL_ENV.getId()))
                .queryParam("volunteer_type", VolunteerType.ADULT.getId())
                .when().get("/recruitment/count")
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(RecruitmentCountResult.class);
        assertThat(response.getTotalCnt()).isEqualTo(2);
    }

}
