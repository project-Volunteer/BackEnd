package project.volunteer.domain.signup.dto;

import lombok.Builder;
import lombok.Data;

@Data
public class SaveUserInfoDTO {
    private String nickName;
    private String profile;
    private String email;
    private String birthday;
    private int gender;
    
    @Builder
    public SaveUserInfoDTO(String nickName, String profile, String email, String birthday, int gender) {
        this.nickName = nickName;
        this.profile = profile;
        this.email = email;
        this.birthday = birthday;
        this.gender = gender;
    }

}
