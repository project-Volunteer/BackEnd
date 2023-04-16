package project.volunteer.global.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import project.volunteer.domain.security.PrincipalDetails;
import project.volunteer.domain.user.domain.User;

public class SecurityUtil {

    public static Long getLoginUserNo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        PrincipalDetails principal = (PrincipalDetails) authentication.getPrincipal();
        User user = principal.getUser();
        return user.getUserNo();
    }
}
