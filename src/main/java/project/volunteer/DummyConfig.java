package project.volunteer;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import project.volunteer.domain.image.dao.ImageRepository;
import project.volunteer.domain.recruitmentParticipation.repository.RecruitmentParticipationRepository;
import project.volunteer.domain.recruitment.repository.RecruitmentRepository;
import project.volunteer.domain.recruitment.repository.RepeatPeriodRepository;
import project.volunteer.domain.scheduleParticipation.repository.ScheduleParticipationRepository;
import project.volunteer.domain.sehedule.application.ScheduleCommandService;
import project.volunteer.domain.sehedule.repository.ScheduleRepository;
import project.volunteer.domain.image.dao.StorageRepository;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.global.jwt.util.JwtProvider;

@Configuration
@RequiredArgsConstructor
public class DummyConfig {

    private final UserRepository userRepository;
    private final RecruitmentRepository recruitmentRepository;
    private final RepeatPeriodRepository repeatPeriodRepository;
    private final ImageRepository imageRepository;
    private final StorageRepository storageRepository;
    private final RecruitmentParticipationRepository participantRepository;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleParticipationRepository scheduleParticipationRepository;
    private final JwtProvider jwtProvider;

    @Bean
//    @Profile("prod")
    public DummyDataInit dummyDataInit(){
        return new DummyDataInit(userRepository, recruitmentRepository, repeatPeriodRepository, imageRepository, storageRepository,participantRepository,
                scheduleRepository, scheduleParticipationRepository, jwtProvider);
    }

}
