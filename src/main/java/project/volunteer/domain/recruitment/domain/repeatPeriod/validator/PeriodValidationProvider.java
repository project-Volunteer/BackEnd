package project.volunteer.domain.recruitment.domain.repeatPeriod.validator;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import project.volunteer.domain.recruitment.domain.repeatPeriod.Period;

@Component
@RequiredArgsConstructor
public class PeriodValidationProvider {
    private final List<PeriodValidation> periodValidations;

    public PeriodValidation getValidator(Period period) {
        return periodValidations.stream()
                .filter(validator -> validator.isSupport(period))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 반복 주기 입니다."));
    }

}
