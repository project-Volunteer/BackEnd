package project.volunteer.global.Interceptor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import project.volunteer.global.common.component.OrganizationComponent;
import project.volunteer.global.util.SecurityUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static project.volunteer.global.Interceptor.OrganizationAuth.*;

@RequiredArgsConstructor
@Component
public class OrganizationAuthInterceptor implements HandlerInterceptor {

    private final OrganizationComponent organizationComponent;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler){

        if(!(handler instanceof HandlerMethod)){
            return true;
        }

        OrganizationAuth organizationAuth = ((HandlerMethod) handler).getMethodAnnotation(OrganizationAuth.class);
        if(organizationAuth==null){
            return true;
        }

        //로그인 사용자 정보(스프링 시큐리티 사용)
        Long loginUserNo = SecurityUtil.getLoginUserNo();

        //봉사 모집글 방장 검증
        if(organizationAuth.auth().equals(Auth.ORGANIZATION_ADMIN)){
            organizationComponent.validRecruitmentOwner(request, loginUserNo);
        }

        //봉사 모집글 팀원 검증
        if(organizationAuth.auth().equals(Auth.ORGANIZATION_TEAM)){
            organizationComponent.validRecruitmentTeam(request, loginUserNo);
        }

        // 댓글 작성자 검증
        if(organizationAuth.auth().equals(Auth.REPLY_WRITER)){
            organizationComponent.validateReplyWriter(request, loginUserNo);
        }
        return true;
    }
}
