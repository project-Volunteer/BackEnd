package project.volunteer.global.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import project.volunteer.global.security.PrincipalDetails;
import project.volunteer.domain.user.domain.User;

public class SecurityUtil {

    // Authentication에서 userNo 가져오기
    public static Long getLoginUserNo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        PrincipalDetails principal = (PrincipalDetails) authentication.getPrincipal();
        return principal.getUser().getUserNo();
    }

    // authentication에서 userId 가져오기
    public static String getLoginUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}
