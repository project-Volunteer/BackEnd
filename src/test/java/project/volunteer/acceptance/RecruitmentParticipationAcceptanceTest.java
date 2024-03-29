package project.volunteer.acceptance;

import static io.restassured.RestAssured.given;
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
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.recruitment.domain.repeatPeriod.Period;
import project.volunteer.domain.recruitment.domain.repeatPeriod.Week;
import project.volunteer.domain.recruitmentParticipation.api.dto.request.ParticipantAddRequest;
import project.volunteer.domain.recruitmentParticipation.api.dto.request.ParticipantRemoveRequest;
import project.volunteer.global.common.component.HourFormat;

public class RecruitmentParticipationAcceptanceTest extends AcceptanceTest {

    @DisplayName("봉사 모집글 팀원 신청을 정상적으로 성공한다.")
    @Test
    void saveJoin() {
        given(clock.instant()).willReturn(Instant.parse("2024-01-29T10:00:00Z"));

        final Long recruitmentNo = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.EDUCATION, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 1000, VolunteeringType.IRREG, "01-01-2024", "02-01-2024", HourFormat.AM, "10:00",
                10, Period.NONE, Week.NONE, List.of(), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));

        given().log().all()
                .header(AUTHORIZATION_HEADER, soeunToken)
                .when().put("/recruitment/{recruitmentNo}/join", recruitmentNo)
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();
    }

    @DisplayName("봉사 모집글 인원이 가득찬 경우 팀원 신청을 할 수 없다.")
    @Test
    void saveJoinWithFullParticipationNum() {
        given(clock.instant()).willReturn(Instant.parse("2024-01-29T10:00:00Z"));

        final Long recruitmentNo = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.EDUCATION, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 2, VolunteeringType.IRREG, "01-01-2024", "02-01-2024", HourFormat.AM, "10:00",
                10, Period.NONE, Week.NONE, List.of(), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));

        final Long recruitmentParticipationNo1 = 봉사_게시물_팀원_가입_요청(soeunToken, recruitmentNo);
        final Long recruitmentParticipationNo2 = 봉사_게시물_팀원_가입_요청(changHoeunToken, recruitmentNo);

        final ParticipantAddRequest request = new ParticipantAddRequest(
                List.of(recruitmentParticipationNo1, recruitmentParticipationNo2));
        봉사_게시물_팀원_가입_승인(bonsikToken, recruitmentNo, request);

        given().log().all()
                .header(AUTHORIZATION_HEADER, bongbongToken)
                .when().put("/recruitment/{recruitmentNo}/join", recruitmentNo)
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract();
    }

    @DisplayName("이미 팀원 신청한 회원은 가입 신청을 할 수 없다.")
    @Test
    void saveJoinDuplicate() {
        given(clock.instant()).willReturn(Instant.parse("2024-01-29T10:00:00Z"));

        final Long recruitmentNo = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.EDUCATION, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 10, VolunteeringType.IRREG, "01-01-2024", "02-01-2024", HourFormat.AM, "10:00",
                10, Period.NONE, Week.NONE, List.of(), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));

        봉사_게시물_팀원_가입_요청(soeunToken, recruitmentNo);

        given().log().all()
                .header(AUTHORIZATION_HEADER, soeunToken)
                .when().put("/recruitment/{recruitmentNo}/join", recruitmentNo)
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract();
    }

    @DisplayName("신청 기간이 종료된 봉사 모집글에는 가입 신청을 할 수 없다.")
    @Test
    void saveJoinWithDoneRecruitment() {
        given(clock.instant()).willReturn(Instant.parse("2024-02-02T10:00:00Z"));

        final Long recruitmentNo = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.EDUCATION, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 10, VolunteeringType.IRREG, "01-01-2024", "02-01-2024", HourFormat.AM, "10:00",
                10, Period.NONE, Week.NONE, List.of(), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));

        given().log().all()
                .header(AUTHORIZATION_HEADER, soeunToken)
                .when().put("/recruitment/{recruitmentNo}/join", recruitmentNo)
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract();
    }

    @DisplayName("봉사 모집글 팀원 신청 취소를 정상적으로 성공한다.")
    @Test
    void cancelJoin() {
        given(clock.instant()).willReturn(Instant.parse("2024-01-29T10:00:00Z"));

        final Long recruitmentNo = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.EDUCATION, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 1000, VolunteeringType.IRREG, "01-01-2024", "02-01-2024", HourFormat.AM, "10:00",
                10, Period.NONE, Week.NONE, List.of(), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));

        봉사_게시물_팀원_가입_요청(soeunToken, recruitmentNo);

        given().log().all()
                .header(AUTHORIZATION_HEADER, soeunToken)
                .when().put("/recruitment/{recruitmentNo}/cancel", recruitmentNo)
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();
    }

    @DisplayName("신청 기간이 종료된 봉사 모집글에는 참여 신청 취소를 할 수 없다.")
    @Test
    void cancelJoinWithDoneRecruitment() {
        given(clock.instant()).willReturn(Instant.parse("2024-01-29T10:00:00Z"));

        final Long recruitmentNo = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.EDUCATION, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 1000, VolunteeringType.IRREG, "01-01-2024", "02-01-2024", HourFormat.AM, "10:00",
                10, Period.NONE, Week.NONE, List.of(), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));

        봉사_게시물_팀원_가입_요청(soeunToken, recruitmentNo);

        given(clock.instant()).willReturn(Instant.parse("2024-02-02T10:00:00Z"));

        given().log().all()
                .header(AUTHORIZATION_HEADER, soeunToken)
                .when().put("/recruitment/{recruitmentNo}/cancel", recruitmentNo)
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract();
    }

    @DisplayName("봉사 모집글 팀원 신청을 하지 않은 회원은 참여 신청 취소를 할 수 없다.")
    @Test
    void cancelJoinWithNotJoinRequest() {
        given(clock.instant()).willReturn(Instant.parse("2024-01-29T10:00:00Z"));

        final Long recruitmentNo = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.EDUCATION, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 1000, VolunteeringType.IRREG, "01-01-2024", "02-01-2024", HourFormat.AM, "10:00",
                10, Period.NONE, Week.NONE, List.of(), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));

        given().log().all()
                .header(AUTHORIZATION_HEADER, soeunToken)
                .when().put("/recruitment/{recruitmentNo}/cancel", recruitmentNo)
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract();
    }

    @DisplayName("봉사 모집글 팀원 승인된 회원은 탐여 신청 취소를 할 수 없다.")
    @Test
    void cancelJoinWithInvalidState() {
        given(clock.instant()).willReturn(Instant.parse("2024-01-29T10:00:00Z"));

        final Long recruitmentNo = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.EDUCATION, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 1000, VolunteeringType.IRREG, "01-01-2024", "02-01-2024", HourFormat.AM, "10:00",
                10, Period.NONE, Week.NONE, List.of(), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));

        final Long recruitmentParticipationNo1 = 봉사_게시물_팀원_가입_요청(soeunToken, recruitmentNo);

        final ParticipantAddRequest request = new ParticipantAddRequest(List.of(recruitmentParticipationNo1));
        봉사_게시물_팀원_가입_승인(bonsikToken, recruitmentNo, request);

        given().log().all()
                .header(AUTHORIZATION_HEADER, soeunToken)
                .when().put("/recruitment/{recruitmentNo}/cancel", recruitmentNo)
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract();
    }

    @DisplayName("봉사 모집글 팀원 신청 승인에 성공한다.")
    @Test
    void approveJoin() {
        given(clock.instant()).willReturn(Instant.parse("2024-01-29T10:00:00Z"));

        final Long recruitmentNo = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.EDUCATION, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 100, VolunteeringType.IRREG, "01-01-2024", "02-01-2024", HourFormat.AM, "10:00",
                10, Period.NONE, Week.NONE, List.of(), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));

        final Long recruitmentParticipationNo1 = 봉사_게시물_팀원_가입_요청(soeunToken, recruitmentNo);
        final Long recruitmentParticipationNo2 = 봉사_게시물_팀원_가입_요청(changHoeunToken, recruitmentNo);

        given(clock.instant()).willReturn(Instant.parse("2024-02-10T10:00:00Z"));

        final ParticipantAddRequest request = new ParticipantAddRequest(
                List.of(recruitmentParticipationNo1, recruitmentParticipationNo2));

        given().log().all()
                .header(AUTHORIZATION_HEADER, bonsikToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when().put("/recruitment/{recruitmentNo}/approval", recruitmentNo)
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();
    }

    @DisplayName("봉사 모집글 방장이 아니면 신청 승인을 할 수 없다.")
    @Test
    void approveJoinNotOwner() {
        given(clock.instant()).willReturn(Instant.parse("2024-01-29T10:00:00Z"));

        final Long recruitmentNo = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.EDUCATION, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 100, VolunteeringType.IRREG, "01-01-2024", "02-01-2024", HourFormat.AM, "10:00",
                10, Period.NONE, Week.NONE, List.of(), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));

        final Long recruitmentParticipationNo1 = 봉사_게시물_팀원_가입_요청(soeunToken, recruitmentNo);

        given(clock.instant()).willReturn(Instant.parse("2024-02-10T10:00:00Z"));

        final ParticipantAddRequest request = new ParticipantAddRequest(List.of(recruitmentParticipationNo1));

        given().log().all()
                .header(AUTHORIZATION_HEADER, changHoeunToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when().put("/recruitment/{recruitmentNo}/approval", recruitmentNo)
                .then().log().all()
                .statusCode(HttpStatus.FORBIDDEN.value())
                .extract();
    }

    @DisplayName("봉사 모집글 팀원 신청을 하지 않은 회원은 신청 승인을 할 수 없다.")
    @Test
    void approveJoinWithNotJoinRequest() {
        given(clock.instant()).willReturn(Instant.parse("2024-01-29T10:00:00Z"));

        final Long recruitmentNo = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.EDUCATION, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 100, VolunteeringType.IRREG, "01-01-2024", "02-01-2024", HourFormat.AM, "10:00",
                10, Period.NONE, Week.NONE, List.of(), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));

        final Long recruitmentParticipationNo1 = 봉사_게시물_팀원_가입_요청(soeunToken, recruitmentNo);
        봉사_게시물_팀원_가입_취소(soeunToken, recruitmentNo);

        given(clock.instant()).willReturn(Instant.parse("2024-02-10T10:00:00Z"));

        final ParticipantAddRequest request = new ParticipantAddRequest(List.of(recruitmentParticipationNo1));

        given().log().all()
                .header(AUTHORIZATION_HEADER, bonsikToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when().put("/recruitment/{recruitmentNo}/approval", recruitmentNo)
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract();
    }

    @DisplayName("봉사 모집글 최대 참여 인원 이상으로 참여 신청을 승인 하지 못한다.")
    @Test
    void approveJoinWithExceedMaxParticipationNum() {
        given(clock.instant()).willReturn(Instant.parse("2024-01-29T10:00:00Z"));

        final Long recruitmentNo = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.EDUCATION, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 2, VolunteeringType.IRREG, "01-01-2024", "02-01-2024", HourFormat.AM, "10:00",
                10, Period.NONE, Week.NONE, List.of(), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));

        final Long recruitmentParticipationNo1 = 봉사_게시물_팀원_가입_요청(soeunToken, recruitmentNo);
        final Long recruitmentParticipationNo2 = 봉사_게시물_팀원_가입_요청(changHoeunToken, recruitmentNo);
        final Long recruitmentParticipationNo3 = 봉사_게시물_팀원_가입_요청(bongbongToken, recruitmentNo);

        final ParticipantAddRequest request1 = new ParticipantAddRequest(
                List.of(recruitmentParticipationNo1, recruitmentParticipationNo2));
        봉사_게시물_팀원_가입_승인(bonsikToken, recruitmentNo, request1);

        final ParticipantAddRequest request2 = new ParticipantAddRequest(List.of(recruitmentParticipationNo3));

        given().log().all()
                .header(AUTHORIZATION_HEADER, bonsikToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request2)
                .when().put("/recruitment/{recruitmentNo}/approval", recruitmentNo)
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract();
    }

    @DisplayName("봉사 모집글 팀원 방출에 성공한다.")
    @Test
    void deport() {
        given(clock.instant()).willReturn(Instant.parse("2024-01-29T10:00:00Z"));

        final Long recruitmentNo = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.EDUCATION, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 2, VolunteeringType.IRREG, "01-01-2024", "02-01-2024", HourFormat.AM, "10:00",
                10, Period.NONE, Week.NONE, List.of(), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));

        final Long recruitmentParticipationNo1 = 봉사_게시물_팀원_가입_요청(soeunToken, recruitmentNo);

        final ParticipantAddRequest approvalRequest = new ParticipantAddRequest(List.of(recruitmentParticipationNo1));
        봉사_게시물_팀원_가입_승인(bonsikToken, recruitmentNo, approvalRequest);

        final ParticipantRemoveRequest deportRequest = new ParticipantRemoveRequest(List.of(recruitmentParticipationNo1));

        given().log().all()
                .header(AUTHORIZATION_HEADER, bonsikToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(deportRequest)
                .when().put("/recruitment/{recruitmentNo}/kick", recruitmentNo)
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();
    }

    @DisplayName("봉사 모집글 방장이 아니면 팀원 방출을 할 수 없다.")
    @Test
    void deportNotOwner() {
        given(clock.instant()).willReturn(Instant.parse("2024-01-29T10:00:00Z"));

        final Long recruitmentNo = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.EDUCATION, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 2, VolunteeringType.IRREG, "01-01-2024", "02-01-2024", HourFormat.AM, "10:00",
                10, Period.NONE, Week.NONE, List.of(), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));

        final Long recruitmentParticipationNo1 = 봉사_게시물_팀원_가입_요청(soeunToken, recruitmentNo);

        final ParticipantAddRequest approvalRequest = new ParticipantAddRequest(List.of(recruitmentParticipationNo1));
        봉사_게시물_팀원_가입_승인(bonsikToken, recruitmentNo, approvalRequest);

        final ParticipantRemoveRequest deportRequest = new ParticipantRemoveRequest(List.of(recruitmentParticipationNo1));

        given().log().all()
                .header(AUTHORIZATION_HEADER, soeunToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(deportRequest)
                .when().put("/recruitment/{recruitmentNo}/kick", recruitmentNo)
                .then().log().all()
                .statusCode(HttpStatus.FORBIDDEN.value())
                .extract();
    }

    @DisplayName("봉사 모집글 신청 승인된 팀원이 아니면 방출을 할 수 없다.")
    @Test
    void deportWithNotApprovalJoin() {
        given(clock.instant()).willReturn(Instant.parse("2024-01-29T10:00:00Z"));

        final Long recruitmentNo = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.EDUCATION, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 2, VolunteeringType.IRREG, "01-01-2024", "02-01-2024", HourFormat.AM, "10:00",
                10, Period.NONE, Week.NONE, List.of(), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));

        final Long recruitmentParticipationNo1 = 봉사_게시물_팀원_가입_요청(soeunToken, recruitmentNo);

        final ParticipantRemoveRequest deportRequest = new ParticipantRemoveRequest(List.of(recruitmentParticipationNo1));

        given().log().all()
                .header(AUTHORIZATION_HEADER, bonsikToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(deportRequest)
                .when().put("/recruitment/{recruitmentNo}/kick", recruitmentNo)
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract();
    }

}
