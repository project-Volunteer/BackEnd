package project.volunteer.global.jwt.util;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import project.volunteer.global.jwt.dto.JwtToken;

@SpringBootTest
class JwtProviderTest {

    @Autowired
    JwtProvider jwtProvider;

    @Test
    void test() {
        JwtToken jwtToken = jwtProvider.createJwtToken("11111");
        System.out.println(jwtToken.getAccessToken());

        JwtToken jwtToken1 = jwtProvider.createJwtToken("10000");
        System.out.println(jwtToken1.getAccessToken());
    }

}