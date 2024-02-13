package project.volunteer.acceptance;

import static io.restassured.RestAssured.given;
import static project.volunteer.acceptance.AcceptanceFixtures.봉사_게시물_등록;

import java.io.File;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.recruitment.domain.repeatPeriod.Day;
import project.volunteer.domain.recruitment.domain.repeatPeriod.Period;
import project.volunteer.domain.recruitment.domain.repeatPeriod.Week;
import project.volunteer.global.common.component.HourFormat;

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

}
