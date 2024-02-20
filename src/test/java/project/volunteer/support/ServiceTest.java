package project.volunteer.support;

import java.time.Clock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.participation.dao.ParticipantRepository;
import project.volunteer.domain.recruitment.dao.RecruitmentRepository;
import project.volunteer.domain.scheduleParticipation.repository.ScheduleParticipationRepository;
import project.volunteer.domain.sehedule.application.ScheduleCommandUseCase;
import project.volunteer.domain.sehedule.application.ScheduleQueryUseCase;
import project.volunteer.domain.sehedule.repository.ScheduleRepository;
import project.volunteer.domain.user.dao.UserRepository;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public abstract class ServiceTest {
    @Autowired
    protected ScheduleRepository scheduleRepository;

    @Autowired
    protected RecruitmentRepository recruitmentRepository;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected ParticipantRepository participantRepository;

    @Autowired
    protected ScheduleParticipationRepository scheduleParticipationRepository;

    @Autowired
    protected ScheduleCommandUseCase scheduleCommandUseCase;

    @Autowired
    protected ScheduleQueryUseCase scheduleQueryUseCase;

    @SpyBean
    protected Clock clock;

}
