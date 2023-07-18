package project.volunteer.global.Interceptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface OrganizationAuth {

    Auth auth();

    enum Auth{
        ORGANIZATION_ADMIN, //봉사 모집글 방장
        ORGANIZATION_TEAM, //봉사 모집글 팀원
        REPLY_WRITER // 댓글 작성자
    }
}
