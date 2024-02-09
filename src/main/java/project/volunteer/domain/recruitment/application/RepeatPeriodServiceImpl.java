package project.volunteer.domain.recruitment.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.repository.RepeatPeriodRepository;
import project.volunteer.domain.recruitment.application.dto.command.RepeatPeriodCreateCommand;

import java.util.List;
import project.volunteer.domain.recruitment.domain.repeatPeriod.RepeatPeriod;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RepeatPeriodServiceImpl implements RepeatPeriodService {
    private final RepeatPeriodRepository repeatPeriodRepository;

    @Transactional
    public void addRepeatPeriod(Recruitment recruitment, RepeatPeriodCreateCommand saveDto) {
        saveDto.getDayOfWeeks().stream().forEach(day -> {
            RepeatPeriod period = RepeatPeriod.builder().week(saveDto.getWeek()).period(saveDto.getPeriod()).day(day).build();
            period.assignRecruitment(recruitment);
            repeatPeriodRepository.save(period);
        });
    }

    @Override
    @Transactional
    public void deleteRepeatPeriod(Long recruitmentNo) {
        List<RepeatPeriod> findPeriod = getRepeatPeriods(recruitmentNo);
        findPeriod.stream()
                .forEach(p -> {
                    //삭제 플래그 처리 및 연관관계 끊기
                    p.setDeleted();
                    p.removeRecruitment();
                });
    }

    private List<RepeatPeriod> getRepeatPeriods(Long recruitmentNo){
        return repeatPeriodRepository.findByRecruitment_RecruitmentNo(recruitmentNo);
    }
}
