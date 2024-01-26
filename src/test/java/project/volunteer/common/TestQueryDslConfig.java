package project.volunteer.common;

import com.querydsl.jpa.impl.JPAQueryFactory;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import project.volunteer.domain.sehedule.dao.ScheduleParticipantQueryDSLDao;

@TestConfiguration
public class TestQueryDslConfig {
    @PersistenceContext
    public EntityManager em;

    @Bean
    public JPAQueryFactory jpaQueryFactory(){
        return new JPAQueryFactory(em);
    }

    @Bean
    public ScheduleParticipantQueryDSLDao scheduleParticipantQueryDSLDao(){
        return new ScheduleParticipantQueryDSLDao(jpaQueryFactory(), em);
    }

}
