package project.volunteer.acceptance;

import static io.restassured.RestAssured.given;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import project.volunteer.domain.participation.api.dto.ParticipantAddParam;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.sehedule.api.dto.request.ScheduleUpsertRequest;
import project.volunteer.domain.sehedule.api.dto.response.ScheduleUpsertResponse;
import project.volunteer.global.common.component.HourFormat;

public class AcceptanceFixtures {
    private static final String AUTHORIZATION_HEADER = "accessToken";

    public static Long 봉사_게시물_등록(String token,
                                 VolunteeringCategory volunteeringCategory, String organization,
                                 String sido, String sigungu, String details, String fullName, Float latitude,
                                 Float longitude, Boolean isIssued, VolunteerType volunteerType,
                                 Integer volunteerNum, VolunteeringType volunteeringType, String startDay,
                                 String endDay, HourFormat hourFormat, String startTime, Integer progressTime,
                                 String period, String week, List<String> days, String title, String content,
                                 Boolean isPublished, Boolean isStaticImage, File file) {

        Integer recruitmentNo = (Integer) given().log().all()
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                .header(AUTHORIZATION_HEADER, token)
                .multiPart("picture.uploadImage", file)
                .formParam("volunteeringCategory", volunteeringCategory.getId())
                .formParam("organizationName", organization)
                .formParam("isIssued", isIssued)
                .formParam("volunteerType", volunteerType.getId())
                .formParam("volunteerNum", volunteerNum)
                .formParam("startDay", startDay)
                .formParam("endDay", endDay)
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
                .formParam("period", period)
                .formParam("week", week)
                .formParam("days", days)
                .formParam("picture.isStaticImage", isStaticImage)
                .when().post("/recruitment")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .as(HashMap.class)
                .get("no");

        return Long.valueOf(recruitmentNo);
    }

    public static ExtractableResponse<Response> 봉사_게시물_팀원_가입_요청(String token, Long recruitmentNo) {
        return given().log().all()
                .header(AUTHORIZATION_HEADER, token)
                .when().put("/recruitment/{recruitmentNo}/join", recruitmentNo)
                .then().log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();
    }

    public static ExtractableResponse<Response> 봉사_게시물_팀원_가입_승인(String token, Long recruitmentNo,
                                                                ParticipantAddParam request) {
        return given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(AUTHORIZATION_HEADER, token)
                .body(request)
                .when().put("/recruitment/{recruitmentNo}/approval", recruitmentNo)
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

}
