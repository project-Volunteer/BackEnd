package project.volunteer.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.recruitment.dao.RecruitmentRepository;
import project.volunteer.domain.sehedule.application.ScheduleCommandUseCase;
import project.volunteer.domain.sehedule.application.ScheduleQueryService;
import project.volunteer.domain.sehedule.application.ScheduleQueryUseCase;
import project.volunteer.domain.sehedule.repository.ScheduleRepository;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public abstract class ServiceTest {
    @Autowired
    protected ScheduleRepository scheduleRepository;

    @Autowired
    protected RecruitmentRepository recruitmentRepository;

    @Autowired
    protected ScheduleCommandUseCase scheduleCommandUseCase;

    @Autowired
    protected ScheduleQueryUseCase scheduleQueryUseCase;

}
