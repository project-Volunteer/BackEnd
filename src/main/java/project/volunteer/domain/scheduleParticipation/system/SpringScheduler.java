package project.volunteer.domain.scheduleParticipation.system;

import java.time.Clock;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.scheduleParticipation.repository.ScheduleParticipationQueryDSLRepositoryImpl;

@Slf4j
@Component
@RequiredArgsConstructor
public class SpringScheduler {
    private final ScheduleParticipationQueryDSLRepositoryImpl scheduleParticipantQueryDSLDao;
    private final Clock clock;

    @Scheduled(cron = "0 1 0 * * * ", zone = "Asia/Seoul") //매 00시 01분 마다 실행
    @Transactional
    public void scheduleCompletionValidation(){
        log.info("마감 일정 참가자 상태 자동 업데이트 스케줄러 시작");

        final LocalDate currentDate = LocalDate.now(clock);
        scheduleParticipantQueryDSLDao.unApprovedCompleteOfAllFinishedScheduleParticipant(currentDate);

        log.info("마감 일정 참가자 상태 자동 업데이트 스케줄러 종료");
    }

}
