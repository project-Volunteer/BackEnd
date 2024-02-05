package project.volunteer.support;

import com.querydsl.jpa.impl.JPAQueryFactory;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import project.volunteer.domain.scheduleParticipation.repository.ScheduleParticipationQueryDSLRepositoryImpl;

@TestConfiguration
public class TestQueryDslConfig {
    @PersistenceContext
    public EntityManager em;

    @Bean
    public JPAQueryFactory jpaQueryFactory(){
        return new JPAQueryFactory(em);
    }

    @Bean
    public ScheduleParticipationQueryDSLRepositoryImpl scheduleParticipantQueryDSLDao(){
        return new ScheduleParticipationQueryDSLRepositoryImpl(jpaQueryFactory(), em);
    }

}
