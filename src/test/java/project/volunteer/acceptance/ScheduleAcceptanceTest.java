package project.volunteer.acceptance;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.BDDMockito.given;
import static project.volunteer.acceptance.AcceptanceFixtures.봉사_게시물_등록;
import static project.volunteer.acceptance.AcceptanceFixtures.봉사_게시물_팀원_가입_승인;
import static project.volunteer.acceptance.AcceptanceFixtures.봉사_게시물_팀원_가입_요청;
import static project.volunteer.acceptance.AcceptanceFixtures.봉사_일정_등록;
import static project.volunteer.acceptance.AcceptanceFixtures.봉사_일정_참여;
import static project.volunteer.acceptance.AcceptanceFixtures.봉사_일정_참여_취소승인;
import static project.volunteer.acceptance.AcceptanceFixtures.봉사_일정_참여_취소요청;
import static project.volunteer.acceptance.AcceptanceFixtures.봉사_일정_참여완료_승인;
import static project.volunteer.acceptance.AcceptanceFixtures.봉사_일정_참여완료_조회;
import static project.volunteer.acceptance.AcceptanceFixtures.봉사_일정_취소요청_조회;
import static project.volunteer.acceptance.AcceptanceFixtures.캘린더_일정_조회;

import java.io.File;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import project.volunteer.domain.participation.api.dto.ParticipantAddParam;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.scheduleParticipation.api.dto.CancelApproval;
import project.volunteer.domain.scheduleParticipation.api.dto.CompleteApproval;
import project.volunteer.domain.scheduleParticipation.service.dto.CancelledParticipantList;
import project.volunteer.domain.scheduleParticipation.service.dto.CompletedParticipantList;
import project.volunteer.domain.sehedule.api.dto.request.ScheduleAddressRequest;
import project.volunteer.domain.sehedule.api.dto.request.ScheduleUpsertRequest;
import project.volunteer.domain.sehedule.api.dto.response.ScheduleCalenderSearchResponse;
import project.volunteer.domain.sehedule.api.dto.response.ScheduleCalenderSearchResponses;
import project.volunteer.domain.sehedule.application.dto.query.ScheduleDetailSearchResult;
import project.volunteer.global.common.component.HourFormat;
import project.volunteer.global.common.dto.StateResult;

public class ScheduleAcceptanceTest extends AcceptanceTest {

    @DisplayName("일정을 정상적으로 등록한다.")
    @Test
    void saveSchedule() {
        final Long recruitmentNo = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.EDUCATION, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 1000, VolunteeringType.IRREG, "01-01-2024", "02-20-2024", HourFormat.AM, "10:00",
                10,
                null, null, List.of(), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));

        final ScheduleUpsertRequest request = new ScheduleUpsertRequest(
                new ScheduleAddressRequest("1", "1111", "1111", "1111"), "02-10-2024", "AM", "10:00", 2,
                "unicef", 100, "content");

        given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(AUTHORIZATION_HEADER, bonsikToken)
                .body(request)
                .when().post("/recruitment/{recruitmentNo}/schedule", recruitmentNo)
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value())
                .extract();
    }

    @DisplayName("방장이 아니면 봉사 모집글에 일정을 등록할 수 없다.")
    @Test
    void saveScheduleNotRecruitmentOwner() {
        final Long recruitmentNo = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.EDUCATION, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 1000, VolunteeringType.IRREG, "01-01-2024", "02-20-2024", HourFormat.AM, "10:00",
                10,
                null, null, List.of(), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));

        final ScheduleUpsertRequest request = new ScheduleUpsertRequest(
                new ScheduleAddressRequest("1", "1111", "1111", "1111"), "02-10-2024", "AM", "10:00", 2,
                "unicef", 100, "content");

        given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(AUTHORIZATION_HEADER, soeunToken)
                .body(request)
                .when().post("/recruitment/{recruitmentNo}/schedule", recruitmentNo)
                .then().log().all()
                .statusCode(HttpStatus.FORBIDDEN.value())
                .extract();
    }

    @DisplayName("일정 모집 인원은 봉사 모집글 모집 인원보다 많을 수 없다. ")
    @Test
    void saveScheduleExceedRecruitmentParticipant() {
        final Long recruitmentNo = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.EDUCATION, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 50, VolunteeringType.IRREG, "01-01-2024", "02-20-2024", HourFormat.AM, "10:00",
                10,
                null, null, List.of(), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));

        final ScheduleUpsertRequest request = new ScheduleUpsertRequest(
                new ScheduleAddressRequest("1", "1111", "1111", "1111"), "02-10-2024", "AM", "10:00", 2,
                "unicef", 51, "content");

        given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(AUTHORIZATION_HEADER, bonsikToken)
                .body(request)
                .when().post("/recruitment/{recruitmentNo}/schedule", recruitmentNo)
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract();
    }

    @DisplayName("일정을 정상적으로 수정한다.")
    @Test
    void editSchedule() {
        final Long recruitmentNo = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.EDUCATION, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 100, VolunteeringType.IRREG, "01-01-2024", "02-20-2024", HourFormat.AM, "10:00",
                10,
                null, null, List.of(), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));

        final ScheduleUpsertRequest insertRequest = new ScheduleUpsertRequest(
                new ScheduleAddressRequest("1", "1111", "1111", "1111"), "02-10-2024", "AM", "10:00", 2,
                "unicef", 50, "content");
        final Long scheduleNo = 봉사_일정_등록(bonsikToken, recruitmentNo, insertRequest);

        final ScheduleUpsertRequest updateRequest = new ScheduleUpsertRequest(
                new ScheduleAddressRequest("3", "333", "333", "333"), "02-10-2024", "PM", "10:00", 2,
                "unicef", 70, "content");

        given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(AUTHORIZATION_HEADER, bonsikToken)
                .body(updateRequest)
                .when().put("/recruitment/{recruitmentNo}/schedule/{scheduleNo}", recruitmentNo, scheduleNo)
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();
    }

    @DisplayName("방장이 아니면 일정을 수정할 수 없다.")
    @Test
    void editScheduleNotRecruitmentOwner() {
        final Long recruitmentNo = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.EDUCATION, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 100, VolunteeringType.IRREG, "01-01-2024", "02-20-2024", HourFormat.AM, "10:00",
                10,
                null, null, List.of(), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));

        final ScheduleUpsertRequest insertRequest = new ScheduleUpsertRequest(
                new ScheduleAddressRequest("1", "1111", "1111", "1111"), "02-10-2024", "AM", "10:00", 2,
                "unicef", 50, "content");
        final Long scheduleNo = 봉사_일정_등록(bonsikToken, recruitmentNo, insertRequest);

        final ScheduleUpsertRequest updateRequest = new ScheduleUpsertRequest(
                new ScheduleAddressRequest("3", "333", "333", "333"), "02-10-2024", "PM", "10:00", 2,
                "unicef", 70, "content");

        given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(AUTHORIZATION_HEADER, soeunToken)
                .body(updateRequest)
                .when().put("/recruitment/{recruitmentNo}/schedule/{scheduleNo}", recruitmentNo, scheduleNo)
                .then().log().all()
                .statusCode(HttpStatus.FORBIDDEN.value())
                .extract();
    }

    @DisplayName("일정 수정 시 일정 모집 인원은 봉사 모집글 모집 인원보다 많을 수 없다. ")
    @Test
    void editScheduleExceedRecruitmentParticipant() {
        final Long recruitmentNo = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.EDUCATION, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 50, VolunteeringType.IRREG, "01-01-2024", "02-20-2024", HourFormat.AM, "10:00",
                10,
                null, null, List.of(), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));

        final ScheduleUpsertRequest insertRequest = new ScheduleUpsertRequest(
                new ScheduleAddressRequest("1", "1111", "1111", "1111"), "02-10-2024", "AM", "10:00", 2,
                "unicef", 49, "content");
        final Long scheduleNo = 봉사_일정_등록(bonsikToken, recruitmentNo, insertRequest);

        final ScheduleUpsertRequest updateRequest = new ScheduleUpsertRequest(
                new ScheduleAddressRequest("3", "333", "333", "333"), "02-10-2024", "PM", "10:00", 2,
                "unicef", 52, "content");

        given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(AUTHORIZATION_HEADER, bonsikToken)
                .body(updateRequest)
                .when().put("/recruitment/{recruitmentNo}/schedule/{scheduleNo}", recruitmentNo, scheduleNo)
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract();
    }

    @DisplayName("일정 수정 시 일정 모집 인원은 현재 일정 참여자 수보다 적을 수 없다.")
    @Test
    void editScheduleLessScheduleParticipant() {
        final Long recruitmentNo = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.EDUCATION, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 50, VolunteeringType.IRREG, "01-01-2024", "02-20-2024", HourFormat.AM, "10:00",
                10,
                null, null, List.of(), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));

        봉사_게시물_팀원_가입_요청(soeunToken, recruitmentNo);
        봉사_게시물_팀원_가입_요청(changHoeunToken, recruitmentNo);

        봉사_게시물_팀원_가입_승인(bonsikToken, recruitmentNo, new ParticipantAddParam(List.of(soeunNo, changHoeunNo)));

        final ScheduleUpsertRequest insertRequest = new ScheduleUpsertRequest(
                new ScheduleAddressRequest("1", "1111", "1111", "1111"), "02-10-2024", "AM", "10:00", 2,
                "unicef", 10, "content");
        final Long scheduleNo = 봉사_일정_등록(bonsikToken, recruitmentNo, insertRequest);

        봉사_일정_참여(soeunToken, recruitmentNo, scheduleNo);
        봉사_일정_참여(changHoeunToken, recruitmentNo, scheduleNo);

        final ScheduleUpsertRequest updateRequest = new ScheduleUpsertRequest(
                new ScheduleAddressRequest("3", "333", "333", "333"), "02-10-2024", "PM", "10:00", 2,
                "unicef", 1, "content");

        given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(AUTHORIZATION_HEADER, bonsikToken)
                .body(updateRequest)
                .when().put("/recruitment/{recruitmentNo}/schedule/{scheduleNo}", recruitmentNo, scheduleNo)
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @DisplayName("봉사 모집글에 존재하는 일정들 중 참여가 가능한 가장 가까운 일정 조회에 성공한다.")
    @Test
    void findClosestSchedule() {
        final Long recruitmentNo = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.EDUCATION, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 100, VolunteeringType.IRREG, "01-01-2024", "02-20-2024", HourFormat.AM, "10:00",
                10,
                null, null, List.of(), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));

        final ScheduleUpsertRequest insertRequest1 = new ScheduleUpsertRequest(
                new ScheduleAddressRequest("1", "1111", "1111", "1111"), "02-01-2024", "AM", "10:00", 2,
                "unicef", 50, "content");
        final ScheduleUpsertRequest insertRequest2 = new ScheduleUpsertRequest(
                new ScheduleAddressRequest("1", "1111", "1111", "1111"), "02-03-2024", "AM", "10:00", 2,
                "unicef", 50, "content");
        final ScheduleUpsertRequest insertRequest3 = new ScheduleUpsertRequest(
                new ScheduleAddressRequest("1", "1111", "1111", "1111"), "02-04-2024", "AM", "10:00", 2,
                "unicef", 50, "content");

        final Long scheduleNo1 = 봉사_일정_등록(bonsikToken, recruitmentNo, insertRequest1);
        final Long scheduleNo2 = 봉사_일정_등록(bonsikToken, recruitmentNo, insertRequest2);
        final Long scheduleNo3 = 봉사_일정_등록(bonsikToken, recruitmentNo, insertRequest3);

        given(clock.instant()).willReturn(Instant.parse("2024-02-03T10:00:00Z"));

        ScheduleDetailSearchResult response = given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(AUTHORIZATION_HEADER, bonsikToken)
                .when().get("/recruitment/{recruitmentNo}/schedule", recruitmentNo)
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(ScheduleDetailSearchResult.class);
        assertAll(
                () -> assertThat(response.hasData()).isTrue(),
                () -> assertThat(response.getNo()).isEqualTo(scheduleNo3)
        );
    }

    @DisplayName("봉사 모집글에 존재하는 일정들 중 참여가 가능한 가장 가까운 일정이 존재하지 않을 경우, hasData컬럼이 false로 나온다.")
    @Test
    void findClosestScheduleNotExist() {
        final Long recruitmentNo = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.EDUCATION, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 100, VolunteeringType.IRREG, "01-01-2024", "02-20-2024", HourFormat.AM, "10:00",
                10,
                null, null, List.of(), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));

        final ScheduleUpsertRequest insertRequest1 = new ScheduleUpsertRequest(
                new ScheduleAddressRequest("1", "1111", "1111", "1111"), "02-01-2024", "AM", "10:00", 2,
                "unicef", 50, "content");
        final ScheduleUpsertRequest insertRequest2 = new ScheduleUpsertRequest(
                new ScheduleAddressRequest("1", "1111", "1111", "1111"), "02-03-2024", "AM", "10:00", 2,
                "unicef", 50, "content");
        final ScheduleUpsertRequest insertRequest3 = new ScheduleUpsertRequest(
                new ScheduleAddressRequest("1", "1111", "1111", "1111"), "02-04-2024", "AM", "10:00", 2,
                "unicef", 50, "content");
        봉사_일정_등록(bonsikToken, recruitmentNo, insertRequest1);
        봉사_일정_등록(bonsikToken, recruitmentNo, insertRequest2);
        봉사_일정_등록(bonsikToken, recruitmentNo, insertRequest3);

        given(clock.instant()).willReturn(Instant.parse("2024-02-04T10:00:00Z"));

        ScheduleDetailSearchResult response = given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(AUTHORIZATION_HEADER, bonsikToken)
                .when().get("/recruitment/{recruitmentNo}/schedule", recruitmentNo)
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(ScheduleDetailSearchResult.class);
        assertThat(response.getHasData()).isFalse();
    }

    @DisplayName("팀원이 아니면 봉사 모집글 일정 페이지를 조회할 수 없다.")
    @Test
    void findClosestScheduleNotRecruitmentTeam() {
        final Long recruitmentNo = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.EDUCATION, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 100, VolunteeringType.IRREG, "01-01-2024", "02-20-2024", HourFormat.AM, "10:00",
                10,
                null, null, List.of(), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));

        final ScheduleUpsertRequest insertRequest = new ScheduleUpsertRequest(
                new ScheduleAddressRequest("1", "1111", "1111", "1111"), "02-02-2024", "AM", "10:00", 2,
                "unicef", 50, "content");
        봉사_일정_등록(bonsikToken, recruitmentNo, insertRequest);

        given(clock.instant()).willReturn(Instant.parse("2024-02-01T10:00:00Z"));

        given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(AUTHORIZATION_HEADER, soeunToken)
                .when().get("/recruitment/{recruitmentNo}/schedule", recruitmentNo)
                .then().log().all()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @DisplayName("캘린더를 통한 일정 상세 정보를 성공적으로 조회한다.")
    @Test
    void findSchedule() {
        final Long recruitmentNo = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.EDUCATION, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 100, VolunteeringType.IRREG, "01-01-2024", "02-20-2024", HourFormat.AM, "10:00",
                10,
                null, null, List.of(), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));

        final ScheduleUpsertRequest insertRequest1 = new ScheduleUpsertRequest(
                new ScheduleAddressRequest("1", "1111", "1111", "1111"), "02-01-2024", "AM", "10:00", 2,
                "unicef", 50, "content");
        final ScheduleUpsertRequest insertRequest2 = new ScheduleUpsertRequest(
                new ScheduleAddressRequest("1", "1111", "1111", "1111"), "02-03-2024", "AM", "10:00", 2,
                "unicef", 50, "content");
        final ScheduleUpsertRequest insertRequest3 = new ScheduleUpsertRequest(
                new ScheduleAddressRequest("1", "1111", "1111", "1111"), "02-04-2024", "AM", "10:00", 2,
                "unicef", 50, "content");
        봉사_일정_등록(bonsikToken, recruitmentNo, insertRequest1);
        봉사_일정_등록(bonsikToken, recruitmentNo, insertRequest2);
        final Long scheduleNo3 = 봉사_일정_등록(bonsikToken, recruitmentNo, insertRequest3);

        final List<ScheduleCalenderSearchResponse> calendarSchedules = 캘린더_일정_조회(bonsikToken, recruitmentNo, 2024, 2);
        final Long lastCalendarScheduleNo = calendarSchedules.get(calendarSchedules.size() - 1).getNo();

        ScheduleDetailSearchResult response = given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(AUTHORIZATION_HEADER, bonsikToken)
                .when().get("/recruitment/{recruitmentNo}/calendar/{scheduleNo}", recruitmentNo, lastCalendarScheduleNo)
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(ScheduleDetailSearchResult.class);
        assertThat(response.getNo()).isEqualTo(scheduleNo3);
    }

    @DisplayName("팀원이 아니면 봉사 모집글 일정 상세 조회할 수 없다.")
    @Test
    void findScheduleNotRecruitmentTeam() {
        final Long recruitmentNo = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.EDUCATION, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 100, VolunteeringType.IRREG, "01-01-2024", "02-20-2024", HourFormat.AM, "10:00",
                10,
                null, null, List.of(), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));

        final ScheduleUpsertRequest insertRequest = new ScheduleUpsertRequest(
                new ScheduleAddressRequest("1", "1111", "1111", "1111"), "02-02-2024", "AM", "10:00", 2,
                "unicef", 50, "content");
        final Long scheduleNo = 봉사_일정_등록(bonsikToken, recruitmentNo, insertRequest);

        given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(AUTHORIZATION_HEADER, soeunToken)
                .when().get("/recruitment/{recruitmentNo}/calendar/{scheduleNo}", recruitmentNo, scheduleNo)
                .then().log().all()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @DisplayName("캘린더를 통한 일정 상세 조회시, 참여가 가능한 일정이면 state필드가 AVAILABLE로 나온다.")
    @Test
    void findScheduleAvailableState() {
        final Long recruitmentNo = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.EDUCATION, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 100, VolunteeringType.IRREG, "01-01-2024", "02-20-2024", HourFormat.AM, "10:00",
                10,
                null, null, List.of(), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));

        final ScheduleUpsertRequest insertRequest1 = new ScheduleUpsertRequest(
                new ScheduleAddressRequest("1", "1111", "1111", "1111"), "02-01-2024", "AM", "10:00", 2,
                "unicef", 50, "content");
        final ScheduleUpsertRequest insertRequest2 = new ScheduleUpsertRequest(
                new ScheduleAddressRequest("1", "1111", "1111", "1111"), "02-03-2024", "AM", "10:00", 2,
                "unicef", 50, "content");
        봉사_일정_등록(bonsikToken, recruitmentNo, insertRequest1);
        final Long scheduleNo2 = 봉사_일정_등록(bonsikToken, recruitmentNo, insertRequest2);

        봉사_게시물_팀원_가입_요청(soeunToken, recruitmentNo);

        봉사_게시물_팀원_가입_승인(bonsikToken, recruitmentNo, new ParticipantAddParam(List.of(soeunNo)));

        final List<ScheduleCalenderSearchResponse> calendarSchedules = 캘린더_일정_조회(soeunToken, recruitmentNo, 2024, 2);
        final Long lastCalendarScheduleNo = calendarSchedules.get(calendarSchedules.size() - 1).getNo();

        given(clock.instant()).willReturn(Instant.parse("2024-02-02T10:00:00Z"));

        ScheduleDetailSearchResult response = given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(AUTHORIZATION_HEADER, soeunToken)
                .when().get("/recruitment/{recruitmentNo}/calendar/{scheduleNo}", recruitmentNo, lastCalendarScheduleNo)
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(ScheduleDetailSearchResult.class);
        assertAll(
                () -> assertThat(response.getNo()).isEqualTo(scheduleNo2),
                () -> assertThat(response.getState()).isEqualTo(StateResult.AVAILABLE.getId())
        );
    }

    @DisplayName("캘린더를 통한 일정 상세 조회시, 모집 기간이 지난 일정이면 state필드가 DONE로 나온다.")
    @Test
    void findScheduleDoneState() {
        final Long recruitmentNo = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.EDUCATION, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 100, VolunteeringType.IRREG, "01-01-2024", "02-20-2024", HourFormat.AM, "10:00",
                10,
                null, null, List.of(), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));

        final ScheduleUpsertRequest insertRequest1 = new ScheduleUpsertRequest(
                new ScheduleAddressRequest("1", "1111", "1111", "1111"), "02-01-2024", "AM", "10:00", 2,
                "unicef", 50, "content");
        final ScheduleUpsertRequest insertRequest2 = new ScheduleUpsertRequest(
                new ScheduleAddressRequest("1", "1111", "1111", "1111"), "02-03-2024", "AM", "10:00", 2,
                "unicef", 50, "content");
        봉사_일정_등록(bonsikToken, recruitmentNo, insertRequest1);
        final Long scheduleNo2 = 봉사_일정_등록(bonsikToken, recruitmentNo, insertRequest2);

        봉사_게시물_팀원_가입_요청(soeunToken, recruitmentNo);

        봉사_게시물_팀원_가입_승인(bonsikToken, recruitmentNo, new ParticipantAddParam(List.of(soeunNo)));

        final List<ScheduleCalenderSearchResponse> calendarSchedules = 캘린더_일정_조회(soeunToken, recruitmentNo, 2024, 2);
        final Long lastCalendarScheduleNo = calendarSchedules.get(calendarSchedules.size() - 1).getNo();

        given(clock.instant()).willReturn(Instant.parse("2024-02-04T10:00:00Z"));

        ScheduleDetailSearchResult response = given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(AUTHORIZATION_HEADER, soeunToken)
                .when().get("/recruitment/{recruitmentNo}/calendar/{scheduleNo}", recruitmentNo, lastCalendarScheduleNo)
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(ScheduleDetailSearchResult.class);
        assertAll(
                () -> assertThat(response.getNo()).isEqualTo(scheduleNo2),
                () -> assertThat(response.getState()).isEqualTo(StateResult.DONE.getId())
        );
    }

    @DisplayName("캘린더를 통한 일정 상세 조회시, 이미 참여중인 일정이면 state필드가 PARTICIPATING로 나온다.")
    @Test
    void findScheduleParticipatingState() {
        given(clock.instant()).willReturn(Instant.parse("2024-02-02T10:00:00Z"));

        final Long recruitmentNo = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.EDUCATION, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 100, VolunteeringType.IRREG, "01-01-2024", "02-20-2024", HourFormat.AM, "10:00",
                10,
                null, null, List.of(), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));

        final ScheduleUpsertRequest insertRequest1 = new ScheduleUpsertRequest(
                new ScheduleAddressRequest("1", "1111", "1111", "1111"), "02-01-2024", "AM", "10:00", 2,
                "unicef", 50, "content");
        final ScheduleUpsertRequest insertRequest2 = new ScheduleUpsertRequest(
                new ScheduleAddressRequest("1", "1111", "1111", "1111"), "02-03-2024", "AM", "10:00", 2,
                "unicef", 50, "content");
        봉사_일정_등록(bonsikToken, recruitmentNo, insertRequest1);
        final Long scheduleNo2 = 봉사_일정_등록(bonsikToken, recruitmentNo, insertRequest2);

        봉사_게시물_팀원_가입_요청(soeunToken, recruitmentNo);

        봉사_게시물_팀원_가입_승인(bonsikToken, recruitmentNo, new ParticipantAddParam(List.of(soeunNo)));

        봉사_일정_참여(soeunToken, recruitmentNo, scheduleNo2);

        final List<ScheduleCalenderSearchResponse> calendarSchedules = 캘린더_일정_조회(soeunToken, recruitmentNo, 2024, 2);
        final Long lastCalendarScheduleNo = calendarSchedules.get(calendarSchedules.size() - 1).getNo();

        ScheduleDetailSearchResult response = given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(AUTHORIZATION_HEADER, soeunToken)
                .when().get("/recruitment/{recruitmentNo}/calendar/{scheduleNo}", recruitmentNo, lastCalendarScheduleNo)
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(ScheduleDetailSearchResult.class);
        assertAll(
                () -> assertThat(response.getNo()).isEqualTo(scheduleNo2),
                () -> assertThat(response.getState()).isEqualTo(StateResult.PARTICIPATING.getId())
        );
    }

    @DisplayName("캘린더를 통한 일정 상세 조회시, 인원이 가득한 일정이면 state필드가 FULL로 나온다.")
    @Test
    void findScheduleFullState() {
        given(clock.instant()).willReturn(Instant.parse("2024-02-02T10:00:00Z"));

        final Long recruitmentNo = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.EDUCATION, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 100, VolunteeringType.IRREG, "01-01-2024", "02-20-2024", HourFormat.AM, "10:00",
                10,
                null, null, List.of(), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));

        final ScheduleUpsertRequest insertRequest1 = new ScheduleUpsertRequest(
                new ScheduleAddressRequest("1", "1111", "1111", "1111"), "02-01-2024", "AM", "10:00", 2,
                "unicef", 50, "content");
        final ScheduleUpsertRequest insertRequest2 = new ScheduleUpsertRequest(
                new ScheduleAddressRequest("1", "1111", "1111", "1111"), "02-03-2024", "AM", "10:00", 2,
                "unicef", 1, "content");
        봉사_일정_등록(bonsikToken, recruitmentNo, insertRequest1);
        final Long scheduleNo2 = 봉사_일정_등록(bonsikToken, recruitmentNo, insertRequest2);

        봉사_게시물_팀원_가입_요청(soeunToken, recruitmentNo);
        봉사_게시물_팀원_가입_요청(changHoeunToken, recruitmentNo);

        봉사_게시물_팀원_가입_승인(bonsikToken, recruitmentNo, new ParticipantAddParam(List.of(soeunNo, changHoeunNo)));

        봉사_일정_참여(soeunToken, recruitmentNo, scheduleNo2);

        final List<ScheduleCalenderSearchResponse> calendarSchedules = 캘린더_일정_조회(changHoeunToken, recruitmentNo, 2024,
                2);
        final Long lastCalendarScheduleNo = calendarSchedules.get(calendarSchedules.size() - 1).getNo();

        ScheduleDetailSearchResult response = given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(AUTHORIZATION_HEADER, changHoeunToken)
                .when().get("/recruitment/{recruitmentNo}/calendar/{scheduleNo}", recruitmentNo, lastCalendarScheduleNo)
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(ScheduleDetailSearchResult.class);
        assertAll(
                () -> assertThat(response.getNo()).isEqualTo(scheduleNo2),
                () -> assertThat(response.getState()).isEqualTo(StateResult.FULL.getId())
        );
    }

    @DisplayName("캘린더를 통한 일정 상세 조회시, 일정 참가 완료 미승인 된 일정이면 state필드가 COMPLETE_UNAPPROVED로 나온다.")
    @Test
    void findScheduleCompleteUnapprovedState() {
        given(clock.instant()).willReturn(Instant.parse("2024-02-02T10:00:00Z"));

        final Long recruitmentNo = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.EDUCATION, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 100, VolunteeringType.IRREG, "01-01-2024", "02-20-2024", HourFormat.AM, "10:00",
                10,
                null, null, List.of(), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));

        final ScheduleUpsertRequest insertRequest1 = new ScheduleUpsertRequest(
                new ScheduleAddressRequest("1", "1111", "1111", "1111"), "02-01-2024", "AM", "10:00", 2,
                "unicef", 50, "content");
        final ScheduleUpsertRequest insertRequest2 = new ScheduleUpsertRequest(
                new ScheduleAddressRequest("1", "1111", "1111", "1111"), "02-03-2024", "AM", "10:00", 2,
                "unicef", 1, "content");
        봉사_일정_등록(bonsikToken, recruitmentNo, insertRequest1);
        final Long scheduleNo2 = 봉사_일정_등록(bonsikToken, recruitmentNo, insertRequest2);

        봉사_게시물_팀원_가입_요청(soeunToken, recruitmentNo);

        봉사_게시물_팀원_가입_승인(bonsikToken, recruitmentNo, new ParticipantAddParam(List.of(soeunNo)));

        봉사_일정_참여(soeunToken, recruitmentNo, scheduleNo2);

        given(clock.instant()).willReturn(Instant.parse("2024-02-10T10:00:00Z"));
        봉사_일정_참여완료_미승인_스케줄링();

        final List<ScheduleCalenderSearchResponse> calendarSchedules = 캘린더_일정_조회(soeunToken, recruitmentNo, 2024,
                2);
        final Long lastCalendarScheduleNo = calendarSchedules.get(calendarSchedules.size() - 1).getNo();

        ScheduleDetailSearchResult response = given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(AUTHORIZATION_HEADER, soeunToken)
                .when().get("/recruitment/{recruitmentNo}/calendar/{scheduleNo}", recruitmentNo, lastCalendarScheduleNo)
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(ScheduleDetailSearchResult.class);
        assertAll(
                () -> assertThat(response.getNo()).isEqualTo(scheduleNo2),
                () -> assertThat(response.getState()).isEqualTo(StateResult.COMPLETE_UNAPPROVED.getId())
        );
    }

    @DisplayName("캘린더를 통한 일정 상세 조회시, 일정 참가 완료 승인 된 일정이면 state필드가 COMPLETE_APPROVED로 나온다.")
    @Test
    void findScheduleCompleteApprovedState() {
        given(clock.instant()).willReturn(Instant.parse("2024-02-02T10:00:00Z"));

        final Long recruitmentNo = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.EDUCATION, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 100, VolunteeringType.IRREG, "01-01-2024", "02-20-2024", HourFormat.AM, "10:00",
                10,
                null, null, List.of(), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));

        final ScheduleUpsertRequest insertRequest1 = new ScheduleUpsertRequest(
                new ScheduleAddressRequest("1", "1111", "1111", "1111"), "02-01-2024", "AM", "10:00", 2,
                "unicef", 50, "content");
        final ScheduleUpsertRequest insertRequest2 = new ScheduleUpsertRequest(
                new ScheduleAddressRequest("1", "1111", "1111", "1111"), "02-03-2024", "AM", "10:00", 2,
                "unicef", 1, "content");
        봉사_일정_등록(bonsikToken, recruitmentNo, insertRequest1);
        final Long scheduleNo2 = 봉사_일정_등록(bonsikToken, recruitmentNo, insertRequest2);

        봉사_게시물_팀원_가입_요청(soeunToken, recruitmentNo);

        봉사_게시물_팀원_가입_승인(bonsikToken, recruitmentNo, new ParticipantAddParam(List.of(soeunNo)));

        봉사_일정_참여(soeunToken, recruitmentNo, scheduleNo2);

        given(clock.instant()).willReturn(Instant.parse("2024-02-10T10:00:00Z"));
        봉사_일정_참여완료_미승인_스케줄링();

        final List<CompletedParticipantList> completedScheduleParticipants =
                봉사_일정_참여완료_조회(bonsikToken, recruitmentNo, scheduleNo2);

        final CompleteApproval completeApprovalRequest = new CompleteApproval(
                completedScheduleParticipants.stream()
                        .map(CompletedParticipantList::getScheduleParticipationNo)
                        .collect(Collectors.toList())
        );
        봉사_일정_참여완료_승인(bonsikToken, recruitmentNo, scheduleNo2, completeApprovalRequest);

        final List<ScheduleCalenderSearchResponse> calendarSchedules = 캘린더_일정_조회(soeunToken, recruitmentNo, 2024, 2);
        final Long lastCalendarScheduleNo = calendarSchedules.get(calendarSchedules.size() - 1).getNo();

        ScheduleDetailSearchResult response = given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(AUTHORIZATION_HEADER, soeunToken)
                .when().get("/recruitment/{recruitmentNo}/calendar/{scheduleNo}", recruitmentNo, lastCalendarScheduleNo)
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(ScheduleDetailSearchResult.class);
        assertAll(
                () -> assertThat(response.getNo()).isEqualTo(scheduleNo2),
                () -> assertThat(response.getState()).isEqualTo(StateResult.COMPLETE_APPROVED.getId())
        );
    }

    @DisplayName("캘린더를 통한 일정 상세 조회시, 참가 취소한 일정이면 state필드가 CANCELLING로 나온다.")
    @Test
    void findScheduleCancellingState() {
        given(clock.instant()).willReturn(Instant.parse("2024-02-02T10:00:00Z"));

        final Long recruitmentNo = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.EDUCATION, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 100, VolunteeringType.IRREG, "01-01-2024", "02-20-2024", HourFormat.AM, "10:00",
                10,
                null, null, List.of(), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));

        final ScheduleUpsertRequest insertRequest1 = new ScheduleUpsertRequest(
                new ScheduleAddressRequest("1", "1111", "1111", "1111"), "02-01-2024", "AM", "10:00", 2,
                "unicef", 50, "content");
        final ScheduleUpsertRequest insertRequest2 = new ScheduleUpsertRequest(
                new ScheduleAddressRequest("1", "1111", "1111", "1111"), "02-03-2024", "AM", "10:00", 2,
                "unicef", 1, "content");
        봉사_일정_등록(bonsikToken, recruitmentNo, insertRequest1);
        final Long scheduleNo2 = 봉사_일정_등록(bonsikToken, recruitmentNo, insertRequest2);

        봉사_게시물_팀원_가입_요청(soeunToken, recruitmentNo);

        봉사_게시물_팀원_가입_승인(bonsikToken, recruitmentNo, new ParticipantAddParam(List.of(soeunNo)));

        봉사_일정_참여(soeunToken, recruitmentNo, scheduleNo2);

        봉사_일정_참여_취소요청(soeunToken, recruitmentNo, scheduleNo2);

        final List<ScheduleCalenderSearchResponse> calendarSchedules = 캘린더_일정_조회(soeunToken, recruitmentNo, 2024, 2);
        final Long lastCalendarScheduleNo = calendarSchedules.get(calendarSchedules.size() - 1).getNo();

        ScheduleDetailSearchResult response = given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(AUTHORIZATION_HEADER, soeunToken)
                .when().get("/recruitment/{recruitmentNo}/calendar/{scheduleNo}", recruitmentNo, lastCalendarScheduleNo)
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(ScheduleDetailSearchResult.class);
        assertAll(
                () -> assertThat(response.getNo()).isEqualTo(scheduleNo2),
                () -> assertThat(response.getState()).isEqualTo(StateResult.CANCELLING.getId())
        );
    }

    @DisplayName("캘린더를 통한 일정 상세 조회시, 참가 취소가 승인된 일정이면 state필드가 AVAILABLE로 나온다.")
    @Test
    void findScheduleAvailableStateAfterApprovalCancel() {
        given(clock.instant()).willReturn(Instant.parse("2024-02-02T10:00:00Z"));

        final Long recruitmentNo = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.EDUCATION, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 100, VolunteeringType.IRREG, "01-01-2024", "02-20-2024", HourFormat.AM, "10:00",
                10,
                null, null, List.of(), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));

        final ScheduleUpsertRequest insertRequest1 = new ScheduleUpsertRequest(
                new ScheduleAddressRequest("1", "1111", "1111", "1111"), "02-01-2024", "AM", "10:00", 2,
                "unicef", 50, "content");
        final ScheduleUpsertRequest insertRequest2 = new ScheduleUpsertRequest(
                new ScheduleAddressRequest("1", "1111", "1111", "1111"), "02-03-2024", "AM", "10:00", 2,
                "unicef", 1, "content");
        봉사_일정_등록(bonsikToken, recruitmentNo, insertRequest1);
        final Long scheduleNo2 = 봉사_일정_등록(bonsikToken, recruitmentNo, insertRequest2);

        봉사_게시물_팀원_가입_요청(soeunToken, recruitmentNo);

        봉사_게시물_팀원_가입_승인(bonsikToken, recruitmentNo, new ParticipantAddParam(List.of(soeunNo)));

        봉사_일정_참여(soeunToken, recruitmentNo, scheduleNo2);

        봉사_일정_참여_취소요청(soeunToken, recruitmentNo, scheduleNo2);

        final List<CancelledParticipantList> cancelledParticipants =
                봉사_일정_취소요청_조회(bonsikToken, recruitmentNo, scheduleNo2);

        final CancelApproval cancelApprovalRequest = new CancelApproval(
                cancelledParticipants.get(0).getScheduleParticipationNo());
        봉사_일정_참여_취소승인(bonsikToken, recruitmentNo, scheduleNo2, cancelApprovalRequest);

        final List<ScheduleCalenderSearchResponse> calendarSchedules = 캘린더_일정_조회(soeunToken, recruitmentNo, 2024, 2);
        final Long lastCalendarScheduleNo = calendarSchedules.get(calendarSchedules.size() - 1).getNo();

        ScheduleDetailSearchResult response = given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(AUTHORIZATION_HEADER, soeunToken)
                .when().get("/recruitment/{recruitmentNo}/calendar/{scheduleNo}", recruitmentNo, lastCalendarScheduleNo)
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(ScheduleDetailSearchResult.class);
        assertAll(
                () -> assertThat(response.getNo()).isEqualTo(scheduleNo2),
                () -> assertThat(response.getState()).isEqualTo(StateResult.AVAILABLE.getId())
        );
    }

    @DisplayName("캘린더를 통한 일정 리스트 정보를 성공적으로 조회한다.")
    @Test
    void findCalendarScheduleList() {
        final Long recruitmentNo = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.EDUCATION, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 100, VolunteeringType.IRREG, "01-01-2024", "02-20-2024", HourFormat.AM, "10:00",
                10,
                null, null, List.of(), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));

        final ScheduleUpsertRequest insertRequest1 = new ScheduleUpsertRequest(
                new ScheduleAddressRequest("1", "1111", "1111", "1111"), "02-01-2024", "AM", "10:00", 2,
                "unicef", 50, "content");
        final ScheduleUpsertRequest insertRequest2 = new ScheduleUpsertRequest(
                new ScheduleAddressRequest("1", "1111", "1111", "1111"), "02-03-2024", "AM", "10:00", 2,
                "unicef", 50, "content");
        final ScheduleUpsertRequest insertRequest3 = new ScheduleUpsertRequest(
                new ScheduleAddressRequest("1", "1111", "1111", "1111"), "02-04-2024", "AM", "10:00", 2,
                "unicef", 50, "content");
        final ScheduleUpsertRequest insertRequest4 = new ScheduleUpsertRequest(
                new ScheduleAddressRequest("1", "1111", "1111", "1111"), "03-01-2024", "AM", "10:00", 2,
                "unicef", 50, "content");
        final Long scheduleNo1 = 봉사_일정_등록(bonsikToken, recruitmentNo, insertRequest1);
        final Long scheduleNo2 = 봉사_일정_등록(bonsikToken, recruitmentNo, insertRequest2);
        final Long scheduleNo3 = 봉사_일정_등록(bonsikToken, recruitmentNo, insertRequest3);
        final Long scheduleNo4 = 봉사_일정_등록(bonsikToken, recruitmentNo, insertRequest4);

        List<ScheduleCalenderSearchResponse> response = given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(AUTHORIZATION_HEADER, bonsikToken)
                .queryParam("year", 2024)
                .queryParam("mon", 2)
                .when().get("/recruitment/{recruitmentNo}/calendar", recruitmentNo)
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(ScheduleCalenderSearchResponses.class)
                .getScheduleList();
        assertThat(response).hasSize(3)
                .extracting("no")
                .containsExactlyInAnyOrder(scheduleNo1, scheduleNo2, scheduleNo3);
    }

}
