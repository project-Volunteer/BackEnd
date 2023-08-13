package project.volunteer.domain.recruitment.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.dao.RepeatPeriodRepository;
import project.volunteer.domain.recruitment.domain.RepeatPeriod;
import project.volunteer.domain.recruitment.application.dto.RepeatPeriodParam;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RepeatPeriodServiceImpl implements RepeatPeriodService {
    private final RepeatPeriodRepository repeatPeriodRepository;

    @Transactional
    public void addRepeatPeriod(Recruitment recruitment, RepeatPeriodParam saveDto) {
        saveDto.getDays().stream().forEach(day -> {
            RepeatPeriod period = RepeatPeriod.builder().week(saveDto.getWeek()).period(saveDto.getPeriod()).day(day).build();
            period.setRecruitment(recruitment);
            repeatPeriodRepository.save(period);
        });
    }

}
