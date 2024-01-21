package project.volunteer.domain.recruitment.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.recruitment.dao.RepeatPeriodRepository;
import project.volunteer.domain.recruitment.application.dto.RepeatPeriod;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RepeatPeriodServiceImpl implements RepeatPeriodService {
    private final RepeatPeriodRepository repeatPeriodRepository;

    @Transactional
    public void addRepeatPeriod(Recruitment recruitment, RepeatPeriod saveDto) {
        saveDto.getDays().stream().forEach(day -> {
            project.volunteer.domain.recruitment.domain.RepeatPeriod period = project.volunteer.domain.recruitment.domain.RepeatPeriod.builder().week(saveDto.getWeek()).period(saveDto.getPeriod()).day(day).build();
            period.setRecruitment(recruitment);
            repeatPeriodRepository.save(period);
        });
    }

    @Override
    @Transactional
    public void deleteRepeatPeriod(Long recruitmentNo) {
        List<project.volunteer.domain.recruitment.domain.RepeatPeriod> findPeriod = getRepeatPeriods(recruitmentNo);
        findPeriod.stream()
                .forEach(p -> {
                    //삭제 플래그 처리 및 연관관계 끊기
                    p.setDeleted();
                    p.removeRecruitment();
                });
    }

    private List<project.volunteer.domain.recruitment.domain.RepeatPeriod> getRepeatPeriods(Long recruitmentNo){
        return repeatPeriodRepository.findByRecruitment_RecruitmentNo(recruitmentNo);
    }
}
