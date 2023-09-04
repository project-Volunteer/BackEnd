package project.volunteer.domain.user.api.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserInfo {
    String nickName;
    String email;
    String profile;

    public UserInfo(String nickName, String email, String profile) {
        this.nickName = nickName;
        this.email = email;
        this.profile = profile;
    }
}
