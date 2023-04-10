package project.volunteer.domain.sehedule.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.recruitment.dao.RecruitmentRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.sehedule.dao.ScheduleRepository;
import project.volunteer.domain.sehedule.domain.Schedule;
import project.volunteer.domain.sehedule.application.dto.SaveScheduleDto;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService{

    private final ScheduleRepository scheduleRepository;
    private final RecruitmentRepository recruitmentRepository;

    @Override
    public Long addSchedule(Long recruitmentNo, SaveScheduleDto dto) {

        //굳이 한번더 조회해야할까?
        Recruitment recruitment = recruitmentRepository.findById(recruitmentNo)
                .orElseThrow(() -> new NullPointerException(String.format("Not found recruitmentNo=[%d]",recruitmentNo)));

        Schedule createSchedule = Schedule.builder()
                .timetable(dto.getTimetable())
                .content(dto.getContent())
                .organizationName(dto.getOrganizationName())
                .sido(dto.getSido())
                .sigungu(dto.getSigungu())
                .details(dto.getDetails())
                .build();
        createSchedule.setRecruitment(recruitment);

        return scheduleRepository.save(createSchedule).getScheduleNo();
    }
}
