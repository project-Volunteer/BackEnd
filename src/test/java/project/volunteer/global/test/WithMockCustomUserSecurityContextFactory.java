package project.volunteer.global.test;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import project.volunteer.domain.user.domain.Gender;
import project.volunteer.domain.user.domain.Role;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.security.PrincipalDetails;

import java.time.LocalDate;

public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {
    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        User loginUser = User.builder()
                .id(annotation.tempValue())
                .password(annotation.tempValue())
                .nickName(annotation.tempValue())
                .email(annotation.tempValue() + "@gmail.com")
                .gender(Gender.M)
                .birthDay(LocalDate.now())
                .picture("picture")
                .joinAlarmYn(true).beforeAlarmYn(true).noticeAlarmYn(true)
                .role(Role.USER)
                .provider("kakao").providerId(annotation.tempValue())
                .build();
        loginUser.setUserNo(Long.MAX_VALUE);

        UserDetails userDetails = new PrincipalDetails(loginUser);

        Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails,
                "",
                userDetails.getAuthorities());
        context.setAuthentication(auth);
        return context;
    }
}
