package project.volunteer.acceptance;

import static io.restassured.RestAssured.given;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import project.volunteer.domain.recruitmentParticipation.api.dto.request.ParticipantAddRequest;
import project.volunteer.domain.recruitmentParticipation.api.dto.response.JoinResponse;
import project.volunteer.domain.recruitment.api.dto.response.RecruitmentSaveResponse;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.recruitment.domain.repeatPeriod.Day;
import project.volunteer.domain.recruitment.domain.repeatPeriod.Period;
import project.volunteer.domain.recruitment.domain.repeatPeriod.Week;
import project.volunteer.domain.scheduleParticipation.api.dto.CancelApproval;
import project.volunteer.domain.scheduleParticipation.api.dto.CancelledParticipantListResponse;
import project.volunteer.domain.scheduleParticipation.api.dto.CompleteApproval;
import project.volunteer.domain.scheduleParticipation.api.dto.CompletedParticipantListResponse;
import project.volunteer.domain.scheduleParticipation.service.dto.CancelledParticipantList;
import project.volunteer.domain.scheduleParticipation.service.dto.CompletedParticipantList;
import project.volunteer.domain.sehedule.api.dto.request.ScheduleUpsertRequest;
import project.volunteer.domain.sehedule.api.dto.response.ScheduleCalenderSearchResponse;
import project.volunteer.domain.sehedule.api.dto.response.ScheduleCalenderSearchResponses;
import project.volunteer.domain.sehedule.api.dto.response.ScheduleUpsertResponse;
import project.volunteer.global.common.component.HourFormat;

public class AcceptanceFixtures {
    private static final String AUTHORIZATION_HEADER = "accessToken";

    public static Long 봉사_게시물_등록(String token,
                                 VolunteeringCategory volunteeringCategory, String organization,
                                 String sido, String sigungu, String details, String fullName, Float latitude,
                                 Float longitude, Boolean isIssued, VolunteerType volunteerType,
                                 Integer maxParticipationNum, VolunteeringType volunteeringType, String startDate,
                                 String endDate, HourFormat hourFormat, String startTime, Integer progressTime,
                                 Period period, Week week, List<Day> days, String title, String content,
                                 Boolean isPublished, Boolean isStaticImage, File file) {

        List<String> dayOfWeeks = days.stream()
                .map(Day::getId)
                .collect(Collectors.toList());

        return given().log().all()
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                .header(AUTHORIZATION_HEADER, token)
                .multiPart("picture.uploadImage", file)
                .formParam("volunteeringCategory", volunteeringCategory.getId())
                .formParam("organizationName", organization)
                .formParam("isIssued", isIssued)
                .formParam("volunteerType", volunteerType.getId())
                .formParam("maxParticipationNum", maxParticipationNum)
                .formParam("startDate", startDate)
                .formParam("endDate", endDate)
                .formParam("hourFormat", hourFormat.getId())
                .formParam("startTime", startTime)
                .formParam("progressTime", progressTime)
                .formParam("title", title)
                .formParam("content", content)
                .formParam("isPublished", isPublished)
                .formParam("address.sido", sido)
                .formParam("address.sigungu", sigungu)
                .formParam("address.details", details)
                .formParam("address.fullName", fullName)
                .formParam("address.latitude", latitude)
                .formParam("address.longitude", longitude)
                .formParam("volunteeringType", volunteeringType.getId())
                .formParam("period", period.getId())
                .formParam("week", week.getId())
                .formParam("dayOfWeeks", dayOfWeeks)
                .formParam("picture.isStaticImage", isStaticImage)
                .when().post("/recruitment")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .as(RecruitmentSaveResponse.class)
                .getNo();
    }

    public static Long 봉사_게시물_팀원_가입_요청(String token, Long recruitmentNo) {
        return given().log().all()
                .header(AUTHORIZATION_HEADER, token)
                .when().put("/recruitment/{recruitmentNo}/join", recruitmentNo)
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(JoinResponse.class)
                .getRecruitmentParticipationNo();
    }

    public static ExtractableResponse<Response> 봉사_게시물_팀원_가입_승인(String token, Long recruitmentNo,
                                                                ParticipantAddRequest request) {
        return given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(AUTHORIZATION_HEADER, token)
                .body(request)
                .when().put("/recruitment/{recruitmentNo}/approval", recruitmentNo)
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();
    }

    public static ExtractableResponse<Response> 봉사_게시물_팀원_가입_취소(String token, Long recruitmentNo) {
        return given().log().all()
                .header(AUTHORIZATION_HEADER, token)
                .when().put("/recruitment/{recruitmentNo}/cancel", recruitmentNo)
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();
    }

    public static Long 봉사_일정_등록(String token, Long recruitmentNo,
                                ScheduleUpsertRequest request) {
        return given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(AUTHORIZATION_HEADER, token)
                .body(request)
                .when().post("/recruitment/{recruitmentNo}/schedule", recruitmentNo)
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .as(ScheduleUpsertResponse.class)
                .getScheduleNo();
    }

    public static ExtractableResponse<Response> 봉사_일정_참여(String token, Long recruitmentNo, Long scheduleNo) {
        return given().log().all()
                .header(AUTHORIZATION_HEADER, token)
                .when().put("/recruitment/{recruitmentNo}/schedule/{scheduleNo}/join", recruitmentNo, scheduleNo)
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();
    }

    public static ExtractableResponse<Response> 봉사_일정_참여_취소요청(String token, Long recruitmentNo, Long scheduleNo) {
        return given().log().all()
                .header(AUTHORIZATION_HEADER, token)
                .when().put("/recruitment/{recruitmentNo}/schedule/{scheduleNo}/cancel", recruitmentNo, scheduleNo)
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();
    }

    public static ExtractableResponse<Response> 봉사_일정_참여_취소승인(String token, Long recruitmentNo, Long scheduleNo,
                                                              CancelApproval request) {
        return given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(AUTHORIZATION_HEADER, token)
                .body(request)
                .when().put("/recruitment/{recruitmentNo}/schedule/{scheduleNo}/cancelling", recruitmentNo, scheduleNo)
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();
    }

    public static List<CompletedParticipantList> 봉사_일정_참여완료_조회(String token, Long recruitmentNo, Long scheduleNo) {
        return given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(AUTHORIZATION_HEADER, token)
                .when().get("/recruitment/{recruitmentNo}/schedule/{scheduleNo}/completion", recruitmentNo, scheduleNo)
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(CompletedParticipantListResponse.class)
                .getDone();
    }

    public static List<CancelledParticipantList> 봉사_일정_취소요청_조회(String token, Long recruitmentNo, Long scheduleNo) {
        return given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(AUTHORIZATION_HEADER, token)
                .when().get("/recruitment/{recruitmentNo}/schedule/{scheduleNo}/cancelling", recruitmentNo, scheduleNo)
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(CancelledParticipantListResponse.class)
                .getCancelling();
    }

    public static ExtractableResponse<Response> 봉사_일정_참여완료_승인(String token, Long recruitmentNo, Long scheduleNo,
                                                              CompleteApproval request) {
        return given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(AUTHORIZATION_HEADER, token)
                .body(request)
                .when().put("/recruitment/{recruitmentNo}/schedule/{scheduleNo}/complete", recruitmentNo, scheduleNo)
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();
    }

    public static List<ScheduleCalenderSearchResponse> 캘린더_일정_조회(String token, Long recruitmentNo, int year,
                                                                 int month) {
        return given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(AUTHORIZATION_HEADER, token)
                .queryParam("year", year)
                .queryParam("mon", month)
                .when().get("/recruitment/{recruitmentNo}/calendar", recruitmentNo)
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(ScheduleCalenderSearchResponses.class)
                .getScheduleList();
    }

}
