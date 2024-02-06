package project.volunteer.acceptance;

import static project.volunteer.acceptance.AcceptanceFixtures.봉사_게시물_등록;
import static project.volunteer.acceptance.AcceptanceFixtures.봉사_게시물_팀원_가입_승인;
import static project.volunteer.acceptance.AcceptanceFixtures.봉사_게시물_팀원_가입_요청;
import static project.volunteer.acceptance.AcceptanceFixtures.봉사_일정_등록;
import static project.volunteer.acceptance.AcceptanceFixtures.봉사_일정_참여;

import io.restassured.RestAssured;
import java.io.File;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import project.volunteer.domain.participation.api.dto.ParticipantAddParam;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.sehedule.api.dto.request.ScheduleAddressRequest;
import project.volunteer.domain.sehedule.api.dto.request.ScheduleUpsertRequest;
import project.volunteer.global.common.component.HourFormat;

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

        RestAssured.given().log().all()
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

        RestAssured.given().log().all()
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

        RestAssured.given().log().all()
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

        RestAssured.given().log().all()
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

        RestAssured.given().log().all()
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

        RestAssured.given().log().all()
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

        RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(AUTHORIZATION_HEADER, bonsikToken)
                .body(updateRequest)
                .when().put("/recruitment/{recruitmentNo}/schedule/{scheduleNo}", recruitmentNo, scheduleNo)
                .then().log().all()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract();
    }

}
