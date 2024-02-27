package project.volunteer.support;

import java.time.Clock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.image.dao.ImageRepository;
import project.volunteer.domain.participation.dao.ParticipantRepository;
import project.volunteer.domain.recruitment.application.RecruitmentCommandUseCase;
import project.volunteer.domain.recruitment.application.RecruitmentQueryUseCase;
import project.volunteer.domain.recruitment.repository.RecruitmentRepository;
import project.volunteer.domain.recruitment.repository.RepeatPeriodRepository;
import project.volunteer.domain.scheduleParticipation.repository.ScheduleParticipationRepository;
import project.volunteer.domain.sehedule.application.ScheduleCommandUseCase;
import project.volunteer.domain.sehedule.application.ScheduleQueryUseCase;
import project.volunteer.domain.sehedule.repository.ScheduleRepository;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.global.infra.s3.FileService;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public abstract class ServiceTest {
    @Autowired
    protected ScheduleRepository scheduleRepository;

    @Autowired
    protected RecruitmentRepository recruitmentRepository;

    @Autowired
    protected RepeatPeriodRepository repeatPeriodRepository;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected ParticipantRepository participantRepository;

    @Autowired
    protected ScheduleParticipationRepository scheduleParticipationRepository;

    @Autowired
    protected RecruitmentCommandUseCase recruitmentCommandUseCase;

    @Autowired
    protected RecruitmentQueryUseCase recruitmentQueryUseCase;

    @Autowired
    protected ScheduleCommandUseCase scheduleCommandUseCase;

    @Autowired
    protected ScheduleQueryUseCase scheduleQueryUseCase;


    @SpyBean
    protected Clock clock;

    @Autowired
    protected ImageRepository imageRepository;

    @MockBean
    protected FileService fileService;

}
