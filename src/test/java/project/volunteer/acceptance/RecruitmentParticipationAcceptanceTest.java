package project.volunteer.acceptance;

import static io.restassured.RestAssured.given;
import static org.mockito.BDDMockito.given;
import static project.volunteer.acceptance.AcceptanceFixtures.봉사_게시물_등록;
import static project.volunteer.acceptance.AcceptanceFixtures.봉사_게시물_팀원_가입_승인;
import static project.volunteer.acceptance.AcceptanceFixtures.봉사_게시물_팀원_가입_요청;

import java.io.File;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.domain.recruitment.domain.VolunteeringType;
import project.volunteer.domain.recruitment.domain.repeatPeriod.Period;
import project.volunteer.domain.recruitment.domain.repeatPeriod.Week;
import project.volunteer.domain.recruitmentParticipation.api.dto.request.ParticipantAddRequest;
import project.volunteer.global.common.component.HourFormat;

public class RecruitmentParticipationAcceptanceTest extends AcceptanceTest {

    @DisplayName("봉사 모집글 팀원 신청을 정상적으로 성공한다.")
    @Test
    void saveJoin() {
        given(clock.instant()).willReturn(Instant.parse("2024-01-29T10:00:00Z"));

        Long recruitmentNo = 봉사_게시물_등록(bonsikToken,
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
    void saveJoinFullParticipationNum() {
        given(clock.instant()).willReturn(Instant.parse("2024-01-29T10:00:00Z"));

        Long recruitmentNo = 봉사_게시물_등록(bonsikToken,
                VolunteeringCategory.EDUCATION, "unicef", "11", "1111", "detail", "fullName", 3.2F, 3.2F, true,
                VolunteerType.ADULT, 2, VolunteeringType.IRREG, "01-01-2024", "02-01-2024", HourFormat.AM, "10:00",
                10, Period.NONE, Week.NONE, List.of(), "title", "content", true, false,
                new File("src/main/resources/static/test/file.PNG"));

        Long recruitmentParticipationNo1 = 봉사_게시물_팀원_가입_요청(soeunToken, recruitmentNo);
        Long recruitmentParticipationNo2 = 봉사_게시물_팀원_가입_요청(changHoeunToken, recruitmentNo);

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

        Long recruitmentNo = 봉사_게시물_등록(bonsikToken,
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
    void saveJoinDoneRecruitment() {
        given(clock.instant()).willReturn(Instant.parse("2024-02-02T10:00:00Z"));

        Long recruitmentNo = 봉사_게시물_등록(bonsikToken,
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

}
