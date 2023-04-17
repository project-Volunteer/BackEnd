package project.volunteer.global.jwt.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JwtToken {

    private String accessToken; //엑세스 토큰 헤더 key
    private String refreshToken; //리프레쉬 토큰 헤더 key

}