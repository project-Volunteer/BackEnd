package project.volunteer.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import project.volunteer.domain.image.dao.ImageRepository;
import project.volunteer.domain.recruitmentParticipation.repository.ParticipantRepository;
import project.volunteer.domain.recruitment.repository.RecruitmentRepository;
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


    @Autowired
    protected ImageRepository imageRepository;

}
