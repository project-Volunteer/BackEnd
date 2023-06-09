package project.volunteer.domain.repeatPeriod.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.recruitment.dao.RecruitmentRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.repeatPeriod.dao.RepeatPeriodRepository;
import project.volunteer.domain.repeatPeriod.domain.RepeatPeriod;
import project.volunteer.domain.repeatPeriod.application.dto.RepeatPeriodParam;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RepeatPeriodServiceImpl implements RepeatPeriodService {

    private final RecruitmentRepository recruitmentRepository;
    private final RepeatPeriodRepository repeatPeriodRepository;

    @Transactional
    public void addRepeatPeriod(Long recruitmentNo, RepeatPeriodParam saveDto) {

        //굳이 한번더 조회해야할까?
        Recruitment recruitment = recruitmentRepository.findById(recruitmentNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_RECRUITMENT, String.format("Recruitment No = [%d]", recruitmentNo)));

        saveDto.getDays().stream().forEach(day -> {
            RepeatPeriod period = RepeatPeriod.builder().week(saveDto.getWeek()).period(saveDto.getPeriod()).day(day).build();
            period.setRecruitment(recruitment);
            repeatPeriodRepository.save(period);
        });
    }

}
