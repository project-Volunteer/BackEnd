package project.volunteer.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import project.volunteer.domain.participation.dao.ParticipantRepository;
import project.volunteer.domain.recruitment.dao.RecruitmentRepository;
import project.volunteer.domain.scheduleParticipation.repository.ScheduleParticipationRepository;
import project.volunteer.domain.sehedule.repository.ScheduleRepository;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.global.config.JpaAuditingConfig;
import project.volunteer.global.config.QueryDslConfig;

@DataJpaTest
@ActiveProfiles("test")
@Import({QueryDslConfig.class, JpaAuditingConfig.class})
public abstract class RepositoryTest {
    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected ScheduleRepository scheduleRepository;

    @Autowired
    protected RecruitmentRepository recruitmentRepository;

    @Autowired
    protected ParticipantRepository participantRepository;

    @Autowired
    protected ScheduleParticipationRepository scheduleParticipationRepository;

}
