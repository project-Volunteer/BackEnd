package project.volunteer.global.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import project.volunteer.global.security.PrincipalDetails;
import project.volunteer.domain.user.domain.User;

public class SecurityUtil {
    public static Long getLoginUserNo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        PrincipalDetails principal = (PrincipalDetails) authentication.getPrincipal();
        User user = principal.getUser();
        return user.getUserNo();
    }

    // authentication에서 userId 가져오기
    public static String getLoginUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}
