package project.volunteer.domain.confirmation.dao;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import project.volunteer.global.common.component.RealWorkCode;

import static project.volunteer.domain.confirmation.domain.QConfirmation.confirmation;

@Repository
@RequiredArgsConstructor
public class ConfirmationRepositoryCustomImpl implements ConfirmationRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    //JPQL Exists 지원하지 않아 QueryDSL로 대체
    //Exists 가 빠른 이유는 내부적으로 limit 1 을 사용하기 때문.(count 는 느림)
    @Override
    public Boolean existsCheck(Long userNo, RealWorkCode code, Long no) {
        Integer fetchOne = jpaQueryFactory
                .selectOne()
                .from(confirmation)
                .where(
                        confirmation.no.eq(no),
                        confirmation.realWorkCode.eq(code),
                        confirmation.user.userNo.eq(userNo))
                .fetchFirst();//limit 1

        return fetchOne != null;
    }
}
