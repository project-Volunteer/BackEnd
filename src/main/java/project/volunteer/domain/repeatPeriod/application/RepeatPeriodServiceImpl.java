package project.volunteer.domain.repeatPeriod.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.recruitment.dao.RecruitmentRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.repeatPeriod.dao.RepeatPeriodRepository;
import project.volunteer.domain.repeatPeriod.domain.RepeatPeriod;
import project.volunteer.domain.repeatPeriod.dto.SaveRepeatPeriodDto;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RepeatPeriodServiceImpl implements RepeatPeriodService {

    private final RecruitmentRepository recruitmentRepository;
    private final RepeatPeriodRepository repeatPeriodRepository;

    @Transactional
    public void addRepeatPeriod(Long recruitmentNo, SaveRepeatPeriodDto saveDto) {

        //굳이 한번더 조회해야할까?
        Recruitment recruitment = recruitmentRepository.findById(recruitmentNo)
                .orElseThrow(() -> new NullPointerException("존재하지 않는 모집 게시물입니다."));

        saveDto.getDays().stream().forEach(day -> {
            RepeatPeriod period = RepeatPeriod.builder().week(saveDto.getWeek()).period(saveDto.getPeriod()).day(day).build();
            period.setRecruitment(recruitment);
            repeatPeriodRepository.save(period);
        });
    }
}
