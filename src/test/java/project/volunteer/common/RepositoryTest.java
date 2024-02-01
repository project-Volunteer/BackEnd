package project.volunteer.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import project.volunteer.domain.recruitment.dao.RecruitmentRepository;
import project.volunteer.domain.sehedule.repository.ScheduleRepository;
import project.volunteer.global.config.JpaAuditingConfig;
import project.volunteer.global.config.QueryDslConfig;

@DataJpaTest
@ActiveProfiles("test")
@Import({QueryDslConfig.class, JpaAuditingConfig.class})
public abstract class RepositoryTest {
    @Autowired
    protected ScheduleRepository scheduleRepository;

    @Autowired
    protected RecruitmentRepository recruitmentRepository;

}
