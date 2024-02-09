package project.volunteer.domain.sehedule.application.timetableCreator;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import project.volunteer.domain.recruitment.application.dto.command.RepeatPeriodCreateCommand;

@Component
@RequiredArgsConstructor
public class RepeatTimetableCreateProvider {
    private final List<RepeatTimetableCreator> strategies;

    public RepeatTimetableCreator getCreator(RepeatPeriodCreateCommand repeatPeriod) {
        return strategies.stream()
                .filter(strategy -> strategy.isSupported(repeatPeriod))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 반복 시간 타입이 존재합니다."));
    }

}
