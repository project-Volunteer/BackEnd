package project.volunteer.acceptance;

import static io.restassured.RestAssured.given;
import static org.mockito.BDDMockito.given;
import static project.volunteer.acceptance.AcceptanceFixtures.봉사_게시물_등록;
import static project.volunteer.acceptance.AcceptanceFixtures.봉사_게시물_팀원_가입_승인;
import static project.volunteer.acceptance.AcceptanceFixtures.봉사_게시물_팀원_가입_요청;
import static project.volunteer.acceptance.AcceptanceFixtures.봉사_일정_등록;
import static project.volunteer.acceptance.AcceptanceFixtures.봉사_일정_참여;
import static project.volunteer.acceptance.AcceptanceFixtures.봉사_일정_참여_취소요청;
import static project.volunteer.acceptance.AcceptanceFixtures.봉사_일정_참여완료_승인;
import static project.volunteer.acceptance.AcceptanceFixtures.봉사_일정_참여완료_조회;
import static project.volunteer.acceptance.AcceptanceFixtures.봉사_일정_취소요청_조회;

import java.io.File;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
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
import project.volunteer.domain.scheduleParticipation.api.dto.CancellationApprovalRequest;
import project.volunteer.domain.scheduleParticipation.api.dto.ParticipationCompletionApproveRequest;
import project.volunteer.domain.scheduleParticipation.service.dto.CancelledParticipantDetail;
import project.volunteer.domain.scheduleParticipation.service.dto.CompletedParticipantDetail;
import project.volunteer.domain.sehedule.api.dto.request.ScheduleAddressRequest;
import project.volunteer.domain.sehedule.api.dto.request.ScheduleUpsertRequest;
import project.volunteer.global.common.component.HourFormat;

public class ScheduleParticipationAcceptanceTest extends AcceptanceTest {

    @DisplayName("봉사 일정 참여에 성공한다.")
    @Test
    void saveParticipation() {
        given(clock.instant()).willReturn(Instant.parse("2024-01-29T10:00:00Z"));

        final Long recruitmentNo = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.EDUCATION, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 10, VolunteeringType.IRREG, "01-01-2024", "02-01-2024", HourFormat.AM, "10:00",
                10, Period.NONE, Week.NONE, List.of(), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));

        final Long recruitmentParticipationNo1 = 봉사_게시물_팀원_가입_요청(soeunToken, recruitmentNo);

        final ParticipantAddRequest participantAddRequest = new ParticipantAddRequest(
                List.of(recruitmentParticipationNo1));
        봉사_게시물_팀원_가입_승인(bonsikToken, recruitmentNo, participantAddRequest);

        given(clock.instant()).willReturn(Instant.parse("2024-02-05T10:00:00Z"));

        final ScheduleUpsertRequest scheduleUpsertRequest = new ScheduleUpsertRequest(
                new ScheduleAddressRequest("1", "1111", "1111", "1111"), "02-10-2024", "AM", "10:00", 2,
                "unicef", 10, "content");
        final Long scheduleNo = 봉사_일정_등록(bonsikToken, recruitmentNo, scheduleUpsertRequest);

        given().log().all()
                .header(AUTHORIZATION_HEADER, soeunToken)
                .when().put("/recruitment/{recruitmentNo}/schedule/{scheduleNo}/join", recruitmentNo, scheduleNo)
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();
    }

    @DisplayName("일정 참여 인원이 가득찬 경우 참여를 할 수 없다.")
    @Test
    void saveParticipationWithFullParticipationNum() {
        given(clock.instant()).willReturn(Instant.parse("2024-01-29T10:00:00Z"));

        final Long recruitmentNo = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.EDUCATION, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 10, VolunteeringType.IRREG, "01-01-2024", "02-01-2024", HourFormat.AM, "10:00",
                10, Period.NONE, Week.NONE, List.of(), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));

        final Long recruitmentParticipationNo1 = 봉사_게시물_팀원_가입_요청(soeunToken, recruitmentNo);
        final Long recruitmentParticipationNo2 = 봉사_게시물_팀원_가입_요청(changHoeunToken, recruitmentNo);
        final Long recruitmentParticipationNo3 = 봉사_게시물_팀원_가입_요청(bongbongToken, recruitmentNo);

        final ParticipantAddRequest participantAddRequest = new ParticipantAddRequest(
                List.of(recruitmentParticipationNo1, recruitmentParticipationNo2, recruitmentParticipationNo3));
        봉사_게시물_팀원_가입_승인(bonsikToken, recruitmentNo, participantAddRequest);

        given(clock.instant()).willReturn(Instant.parse("2024-02-05T10:00:00Z"));

        final ScheduleUpsertRequest scheduleUpsertRequest = new ScheduleUpsertRequest(
                new ScheduleAddressRequest("1", "1111", "1111", "1111"), "02-10-2024", "AM", "10:00", 2,
                "unicef", 2, "content");
        final Long scheduleNo = 봉사_일정_등록(bonsikToken, recruitmentNo, scheduleUpsertRequest);

        봉사_일정_참여(soeunToken, recruitmentNo, scheduleNo);
        봉사_일정_참여(changHoeunToken, recruitmentNo, scheduleNo);

        given().log().all()
                .header(AUTHORIZATION_HEADER, bongbongToken)
                .when().put("/recruitment/{recruitmentNo}/schedule/{scheduleNo}/join", recruitmentNo, scheduleNo)
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract();
    }

    @DisplayName("이미 일정에 참여한 인원은 참여할 수 없다.")
    @Test
    void saveParticipationDuplicate() {
        given(clock.instant()).willReturn(Instant.parse("2024-01-29T10:00:00Z"));

        final Long recruitmentNo = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.EDUCATION, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 10, VolunteeringType.IRREG, "01-01-2024", "02-01-2024", HourFormat.AM, "10:00",
                10, Period.NONE, Week.NONE, List.of(), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));

        final Long recruitmentParticipationNo1 = 봉사_게시물_팀원_가입_요청(soeunToken, recruitmentNo);

        final ParticipantAddRequest participantAddRequest = new ParticipantAddRequest(
                List.of(recruitmentParticipationNo1));
        봉사_게시물_팀원_가입_승인(bonsikToken, recruitmentNo, participantAddRequest);

        given(clock.instant()).willReturn(Instant.parse("2024-02-05T10:00:00Z"));

        final ScheduleUpsertRequest scheduleUpsertRequest = new ScheduleUpsertRequest(
                new ScheduleAddressRequest("1", "1111", "1111", "1111"), "02-10-2024", "AM", "10:00", 2,
                "unicef", 2, "content");
        final Long scheduleNo = 봉사_일정_등록(bonsikToken, recruitmentNo, scheduleUpsertRequest);

        봉사_일정_참여(soeunToken, recruitmentNo, scheduleNo);

        given().log().all()
                .header(AUTHORIZATION_HEADER, soeunToken)
                .when().put("/recruitment/{recruitmentNo}/schedule/{scheduleNo}/join", recruitmentNo, scheduleNo)
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract();
    }

    @DisplayName("시작된 일정에는 참여할 수 없다.")
    @Test
    void saveParticipationWithDoneSchedule() {
        given(clock.instant()).willReturn(Instant.parse("2024-01-29T10:00:00Z"));

        final Long recruitmentNo = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.EDUCATION, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 10, VolunteeringType.IRREG, "01-01-2024", "02-01-2024", HourFormat.AM, "10:00",
                10, Period.NONE, Week.NONE, List.of(), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));

        final Long recruitmentParticipationNo1 = 봉사_게시물_팀원_가입_요청(soeunToken, recruitmentNo);

        final ParticipantAddRequest participantAddRequest = new ParticipantAddRequest(
                List.of(recruitmentParticipationNo1));
        봉사_게시물_팀원_가입_승인(bonsikToken, recruitmentNo, participantAddRequest);

        final ScheduleUpsertRequest scheduleUpsertRequest = new ScheduleUpsertRequest(
                new ScheduleAddressRequest("1", "1111", "1111", "1111"), "02-10-2024", "AM", "10:00", 2,
                "unicef", 2, "content");
        final Long scheduleNo = 봉사_일정_등록(bonsikToken, recruitmentNo, scheduleUpsertRequest);

        given(clock.instant()).willReturn(Instant.parse("2024-02-11T10:00:00Z"));

        given().log().all()
                .header(AUTHORIZATION_HEADER, soeunToken)
                .when().put("/recruitment/{recruitmentNo}/schedule/{scheduleNo}/join", recruitmentNo, scheduleNo)
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract();
    }

    @DisplayName("봉사 모집글 팀원이 아닌 경우 일정에 참여할 수 없다.")
    @Test
    void saveParticipationWithNotTeam() {
        given(clock.instant()).willReturn(Instant.parse("2024-01-29T10:00:00Z"));

        final Long recruitmentNo = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.EDUCATION, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 10, VolunteeringType.IRREG, "01-01-2024", "02-01-2024", HourFormat.AM, "10:00",
                10, Period.NONE, Week.NONE, List.of(), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));

        final ScheduleUpsertRequest scheduleUpsertRequest = new ScheduleUpsertRequest(
                new ScheduleAddressRequest("1", "1111", "1111", "1111"), "02-10-2024", "AM", "10:00", 2,
                "unicef", 2, "content");
        final Long scheduleNo = 봉사_일정_등록(bonsikToken, recruitmentNo, scheduleUpsertRequest);

        given().log().all()
                .header(AUTHORIZATION_HEADER, soeunToken)
                .when().put("/recruitment/{recruitmentNo}/schedule/{scheduleNo}/join", recruitmentNo, scheduleNo)
                .then().log().all()
                .statusCode(HttpStatus.FORBIDDEN.value())
                .extract();
    }

    @DisplayName("봉사 일정 취소 요청에 성공한다.")
    @Test
    void cancelParticipation() {
        given(clock.instant()).willReturn(Instant.parse("2024-01-29T10:00:00Z"));

        final Long recruitmentNo = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.EDUCATION, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 10, VolunteeringType.IRREG, "01-01-2024", "02-01-2024", HourFormat.AM, "10:00",
                10, Period.NONE, Week.NONE, List.of(), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));

        final Long recruitmentParticipationNo1 = 봉사_게시물_팀원_가입_요청(soeunToken, recruitmentNo);

        final ParticipantAddRequest participantAddRequest = new ParticipantAddRequest(
                List.of(recruitmentParticipationNo1));
        봉사_게시물_팀원_가입_승인(bonsikToken, recruitmentNo, participantAddRequest);

        given(clock.instant()).willReturn(Instant.parse("2024-02-05T10:00:00Z"));

        final ScheduleUpsertRequest scheduleUpsertRequest = new ScheduleUpsertRequest(
                new ScheduleAddressRequest("1", "1111", "1111", "1111"), "02-10-2024", "AM", "10:00", 2,
                "unicef", 10, "content");
        final Long scheduleNo = 봉사_일정_등록(bonsikToken, recruitmentNo, scheduleUpsertRequest);

        봉사_일정_참여(soeunToken, recruitmentNo, scheduleNo);

        given().log().all()
                .header(AUTHORIZATION_HEADER, soeunToken)
                .when().put("/recruitment/{recruitmentNo}/schedule/{scheduleNo}/cancel", recruitmentNo, scheduleNo)
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();
    }

    @DisplayName("봉사 일정에 참여하지 않은 인원은 취소가 불가능하다.")
    @Test
    void cancelParticipationWithNotParticipant() {
        given(clock.instant()).willReturn(Instant.parse("2024-01-29T10:00:00Z"));

        final Long recruitmentNo = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.EDUCATION, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 10, VolunteeringType.IRREG, "01-01-2024", "02-01-2024", HourFormat.AM, "10:00",
                10, Period.NONE, Week.NONE, List.of(), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));

        final Long recruitmentParticipationNo1 = 봉사_게시물_팀원_가입_요청(soeunToken, recruitmentNo);

        final ParticipantAddRequest participantAddRequest = new ParticipantAddRequest(
                List.of(recruitmentParticipationNo1));
        봉사_게시물_팀원_가입_승인(bonsikToken, recruitmentNo, participantAddRequest);

        given(clock.instant()).willReturn(Instant.parse("2024-02-05T10:00:00Z"));

        final ScheduleUpsertRequest scheduleUpsertRequest = new ScheduleUpsertRequest(
                new ScheduleAddressRequest("1", "1111", "1111", "1111"), "02-10-2024", "AM", "10:00", 2,
                "unicef", 10, "content");
        final Long scheduleNo = 봉사_일정_등록(bonsikToken, recruitmentNo, scheduleUpsertRequest);

        given().log().all()
                .header(AUTHORIZATION_HEADER, soeunToken)
                .when().put("/recruitment/{recruitmentNo}/schedule/{scheduleNo}/cancel", recruitmentNo, scheduleNo)
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract();
    }

    @DisplayName("시작된 일정일 경우, 취소가 불가능하다.")
    @Test
    void cancelParticipantWithDoneSchedule() {
        given(clock.instant()).willReturn(Instant.parse("2024-01-29T10:00:00Z"));

        final Long recruitmentNo = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.EDUCATION, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 10, VolunteeringType.IRREG, "01-01-2024", "02-01-2024", HourFormat.AM, "10:00",
                10, Period.NONE, Week.NONE, List.of(), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));

        final Long recruitmentParticipationNo1 = 봉사_게시물_팀원_가입_요청(soeunToken, recruitmentNo);

        final ParticipantAddRequest participantAddRequest = new ParticipantAddRequest(
                List.of(recruitmentParticipationNo1));
        봉사_게시물_팀원_가입_승인(bonsikToken, recruitmentNo, participantAddRequest);

        given(clock.instant()).willReturn(Instant.parse("2024-02-05T10:00:00Z"));

        final ScheduleUpsertRequest scheduleUpsertRequest = new ScheduleUpsertRequest(
                new ScheduleAddressRequest("1", "1111", "1111", "1111"), "02-10-2024", "AM", "10:00", 2,
                "unicef", 10, "content");
        final Long scheduleNo = 봉사_일정_등록(bonsikToken, recruitmentNo, scheduleUpsertRequest);

        봉사_일정_참여(soeunToken, recruitmentNo, scheduleNo);

        given(clock.instant()).willReturn(Instant.parse("2024-02-11T10:00:00Z"));

        given().log().all()
                .header(AUTHORIZATION_HEADER, soeunToken)
                .when().put("/recruitment/{recruitmentNo}/schedule/{scheduleNo}/cancel", recruitmentNo, scheduleNo)
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract();
    }

    @DisplayName("봉사 일정 취소 요청 승인에 성공한다.")
    @Test
    void approveCancellation() {
        given(clock.instant()).willReturn(Instant.parse("2024-01-29T10:00:00Z"));

        final Long recruitmentNo = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.EDUCATION, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 10, VolunteeringType.IRREG, "01-01-2024", "02-01-2024", HourFormat.AM, "10:00",
                10, Period.NONE, Week.NONE, List.of(), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));

        final Long recruitmentParticipationNo1 = 봉사_게시물_팀원_가입_요청(soeunToken, recruitmentNo);

        final ParticipantAddRequest participantAddRequest = new ParticipantAddRequest(
                List.of(recruitmentParticipationNo1));
        봉사_게시물_팀원_가입_승인(bonsikToken, recruitmentNo, participantAddRequest);

        given(clock.instant()).willReturn(Instant.parse("2024-02-05T10:00:00Z"));

        final ScheduleUpsertRequest scheduleUpsertRequest = new ScheduleUpsertRequest(
                new ScheduleAddressRequest("1", "1111", "1111", "1111"), "02-10-2024", "AM", "10:00", 2,
                "unicef", 10, "content");
        final Long scheduleNo = 봉사_일정_등록(bonsikToken, recruitmentNo, scheduleUpsertRequest);

        봉사_일정_참여(soeunToken, recruitmentNo, scheduleNo);

        봉사_일정_참여_취소요청(soeunToken, recruitmentNo, scheduleNo);

        final List<CancelledParticipantDetail> cancelledParticipantList = 봉사_일정_취소요청_조회(bonsikToken, recruitmentNo,
                scheduleNo);

        final CancellationApprovalRequest request = new CancellationApprovalRequest(
                cancelledParticipantList.stream()
                        .map(CancelledParticipantDetail::getScheduleParticipationNo)
                        .collect(Collectors.toList())
        );

        given().log().all()
                .header(AUTHORIZATION_HEADER, bonsikToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when().put("/recruitment/{recruitmentNo}/schedule/{scheduleNo}/cancelling", recruitmentNo, scheduleNo)
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();
    }

    @DisplayName("봉사 모집글 방장이 아닐 경우, 취소 요청 승인을 할 수 없다.")
    @Test
    void approveCancellationWithNotOwner() {
        given(clock.instant()).willReturn(Instant.parse("2024-01-29T10:00:00Z"));

        final Long recruitmentNo = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.EDUCATION, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 10, VolunteeringType.IRREG, "01-01-2024", "02-01-2024", HourFormat.AM, "10:00",
                10, Period.NONE, Week.NONE, List.of(), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));

        final Long recruitmentParticipationNo1 = 봉사_게시물_팀원_가입_요청(soeunToken, recruitmentNo);

        final ParticipantAddRequest participantAddRequest = new ParticipantAddRequest(
                List.of(recruitmentParticipationNo1));
        봉사_게시물_팀원_가입_승인(bonsikToken, recruitmentNo, participantAddRequest);

        given(clock.instant()).willReturn(Instant.parse("2024-02-05T10:00:00Z"));

        final ScheduleUpsertRequest scheduleUpsertRequest = new ScheduleUpsertRequest(
                new ScheduleAddressRequest("1", "1111", "1111", "1111"), "02-10-2024", "AM", "10:00", 2,
                "unicef", 10, "content");
        final Long scheduleNo = 봉사_일정_등록(bonsikToken, recruitmentNo, scheduleUpsertRequest);

        봉사_일정_참여(soeunToken, recruitmentNo, scheduleNo);

        봉사_일정_참여_취소요청(soeunToken, recruitmentNo, scheduleNo);

        final List<CancelledParticipantDetail> cancelledParticipantList = 봉사_일정_취소요청_조회(bonsikToken, recruitmentNo,
                scheduleNo);

        final CancellationApprovalRequest request = new CancellationApprovalRequest(
                cancelledParticipantList.stream()
                        .map(CancelledParticipantDetail::getScheduleParticipationNo)
                        .collect(Collectors.toList())
        );

        given().log().all()
                .header(AUTHORIZATION_HEADER, soeunToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when().put("/recruitment/{recruitmentNo}/schedule/{scheduleNo}/cancelling", recruitmentNo, scheduleNo)
                .then().log().all()
                .statusCode(HttpStatus.FORBIDDEN.value())
                .extract();
    }

    @DisplayName("일정 참여 완료 승인에 성공한다.")
    @Test
    void approveParticipationCompletion() {
        given(clock.instant()).willReturn(Instant.parse("2024-01-29T10:00:00Z"));

        final Long recruitmentNo = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.EDUCATION, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 10, VolunteeringType.IRREG, "01-01-2024", "02-01-2024", HourFormat.AM, "10:00",
                10, Period.NONE, Week.NONE, List.of(), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));

        final Long recruitmentParticipationNo1 = 봉사_게시물_팀원_가입_요청(soeunToken, recruitmentNo);

        final ParticipantAddRequest participantAddRequest = new ParticipantAddRequest(
                List.of(recruitmentParticipationNo1));
        봉사_게시물_팀원_가입_승인(bonsikToken, recruitmentNo, participantAddRequest);

        given(clock.instant()).willReturn(Instant.parse("2024-02-05T10:00:00Z"));

        final ScheduleUpsertRequest scheduleUpsertRequest = new ScheduleUpsertRequest(
                new ScheduleAddressRequest("1", "1111", "1111", "1111"), "02-10-2024", "AM", "10:00", 2,
                "unicef", 10, "content");
        final Long scheduleNo = 봉사_일정_등록(bonsikToken, recruitmentNo, scheduleUpsertRequest);

        봉사_일정_참여(soeunToken, recruitmentNo, scheduleNo);

        given(clock.instant()).willReturn(Instant.parse("2024-02-11T10:00:00Z"));
        봉사_일정_참여완료_미승인_스케줄링();

        final List<CompletedParticipantDetail> completedParticipantList = 봉사_일정_참여완료_조회(bonsikToken, recruitmentNo,
                scheduleNo);

        final ParticipationCompletionApproveRequest request = new ParticipationCompletionApproveRequest(
                completedParticipantList.stream()
                        .map(CompletedParticipantDetail::getScheduleParticipationNo)
                        .collect(Collectors.toList())
        );

        given().log().all()
                .header(AUTHORIZATION_HEADER, bonsikToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when().put("/recruitment/{recruitmentNo}/schedule/{scheduleNo}/complete", recruitmentNo, scheduleNo)
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();
    }

    @DisplayName("이미 참여 완료 승인된 인원은 재승인을 할 수 없다.")
    @Test
    void approveParticipationCompletionWithInvalidState() {
        given(clock.instant()).willReturn(Instant.parse("2024-01-29T10:00:00Z"));

        final Long recruitmentNo = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.EDUCATION, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 10, VolunteeringType.IRREG, "01-01-2024", "02-01-2024", HourFormat.AM, "10:00",
                10, Period.NONE, Week.NONE, List.of(), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));

        final Long recruitmentParticipationNo1 = 봉사_게시물_팀원_가입_요청(soeunToken, recruitmentNo);

        final ParticipantAddRequest participantAddRequest = new ParticipantAddRequest(
                List.of(recruitmentParticipationNo1));
        봉사_게시물_팀원_가입_승인(bonsikToken, recruitmentNo, participantAddRequest);

        given(clock.instant()).willReturn(Instant.parse("2024-02-05T10:00:00Z"));

        final ScheduleUpsertRequest scheduleUpsertRequest = new ScheduleUpsertRequest(
                new ScheduleAddressRequest("1", "1111", "1111", "1111"), "02-10-2024", "AM", "10:00", 2,
                "unicef", 10, "content");
        final Long scheduleNo = 봉사_일정_등록(bonsikToken, recruitmentNo, scheduleUpsertRequest);

        봉사_일정_참여(soeunToken, recruitmentNo, scheduleNo);

        given(clock.instant()).willReturn(Instant.parse("2024-02-11T10:00:00Z"));
        봉사_일정_참여완료_미승인_스케줄링();

        final List<CompletedParticipantDetail> completedParticipantList = 봉사_일정_참여완료_조회(bonsikToken, recruitmentNo,
                scheduleNo);

        final ParticipationCompletionApproveRequest approveRequest = new ParticipationCompletionApproveRequest(
                completedParticipantList.stream()
                        .map(CompletedParticipantDetail::getScheduleParticipationNo)
                        .collect(Collectors.toList())
        );
        봉사_일정_참여완료_승인(bonsikToken, recruitmentNo, scheduleNo, approveRequest);

        given().log().all()
                .header(AUTHORIZATION_HEADER, bonsikToken)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(approveRequest)
                .when().put("/recruitment/{recruitmentNo}/schedule/{scheduleNo}/complete", recruitmentNo, scheduleNo)
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract();
    }

}
