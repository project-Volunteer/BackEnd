package project.volunteer.global.common.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import project.volunteer.domain.sehedule.application.ScheduleService;

@Slf4j
@Component
@RequiredArgsConstructor
public class Scheduler {

    private final ScheduleService scheduleService;

    @Scheduled(cron = "0 6 17 * * * ", zone = "Asia/Seoul") //매 00시 01분 마다 실행
    public void scheduleCompletionValidation(){

        log.info("마감 일정 참가자 상태 자동 업데이트 스케줄러 시작");
        scheduleService.finishSchedules();
    }

}
