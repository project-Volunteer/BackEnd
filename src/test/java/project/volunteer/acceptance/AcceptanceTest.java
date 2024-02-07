package project.volunteer.acceptance;

import io.restassured.RestAssured;
import java.time.Clock;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import project.volunteer.domain.signup.api.dto.request.UserSignupRequest;
import project.volunteer.domain.signup.application.UserSignupService;
import project.volunteer.global.jwt.util.JwtProvider;
import project.volunteer.support.DatabaseCleaner;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AcceptanceTest {
    @LocalServerPort
    public int port;

    @Autowired
    protected UserSignupService userSignupService;

    @Autowired
    protected JwtProvider jwtProvider;

    @Autowired
    protected DatabaseCleaner databaseCleaner;

    @SpyBean
    protected Clock clock;

    protected String bonsikToken;
    protected String soeunToken;
    protected Long soeunNo;
    protected String changHoeunToken;
    protected Long changHoeunNo;
    protected final String AUTHORIZATION_HEADER = "accessToken";

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        databaseCleaner.execute();

        // oauth 로그인 및 spring security 를 통한 인증 절차 간 mocking 부분이 많으므로 인수 테스트에서 잠시 제외
        final String providerId1 = "1234";
        userSignupService.addUser(
                new UserSignupRequest("bonsik", "http://www...", "test@email.com", "1999-07-27",
                        1, true, true, true, true, "kakao", providerId1));
        bonsikToken = jwtProvider.createAccessToken("kakao_" + providerId1);

        final String providerId2 = "4567";
        soeunNo = userSignupService.addUser(
                new UserSignupRequest("soeun", "http://www...", "test@email.com", "1999-07-27",
                        1, true, true, true, true, "kakao", providerId2));
        soeunToken = jwtProvider.createAccessToken("kakao_" + providerId2);

        final String providerId3 = "990";
        changHoeunNo = userSignupService.addUser(
                new UserSignupRequest("soeun", "http://www...", "test@email.com", "1999-07-27",
                        1, true, true, true, true, "kakao", providerId3));
        changHoeunToken = jwtProvider.createAccessToken("kakao_" + providerId3);
    }

}
