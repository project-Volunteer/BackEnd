package project.volunteer.domain.user.api.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserInfo {
    String nicName;
    String email;
    String profile;

    public UserInfo(String nicName, String email, String profile) {
        this.nicName = nicName;
        this.email = email;
        this.profile = profile;
    }
}
