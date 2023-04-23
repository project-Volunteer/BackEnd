package project.volunteer.global.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import project.volunteer.global.jwt.util.JwtProvider;
import project.volunteer.global.security.PrincipalDetails;

@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityUtil {


    public static String getLoginUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        UserDetails principal = (UserDetails) authentication.getPrincipal();
        return principal.getUsername();
    }

    // authentication에서 userId 가져오기
    public static String getLoginUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}
