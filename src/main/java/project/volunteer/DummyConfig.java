package project.volunteer;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import project.volunteer.domain.image.dao.ImageRepository;
import project.volunteer.domain.participation.dao.ParticipantRepository;
import project.volunteer.domain.recruitment.repository.RecruitmentRepository;
import project.volunteer.domain.recruitment.repository.RepeatPeriodRepository;
import project.volunteer.domain.scheduleParticipation.repository.ScheduleParticipationRepository;
import project.volunteer.domain.sehedule.repository.ScheduleRepository;
import project.volunteer.domain.image.dao.StorageRepository;
import project.volunteer.domain.user.dao.UserRepository;

@Configuration
@RequiredArgsConstructor
public class DummyConfig {

    private final UserRepository userRepository;
    private final RecruitmentRepository recruitmentRepository;
    private final RepeatPeriodRepository repeatPeriodRepository;
    private final ImageRepository imageRepository;
    private final StorageRepository storageRepository;
    private final ParticipantRepository participantRepository;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleParticipationRepository scheduleParticipationRepository;

    @Bean
    @Profile("prod")
    public DummyDataInit dummyDataInit(){
        return new DummyDataInit(userRepository, recruitmentRepository, repeatPeriodRepository, imageRepository, storageRepository,participantRepository,
                scheduleRepository, scheduleParticipationRepository);
    }
}
