package project.volunteer.domain.user.api.dto.request;

import javax.validation.constraints.NotNull;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoRequestParam {
    @NotNull
    private String nickName;
    
    @NotNull
    private String email;
    
    private MultipartFile profile;
}
