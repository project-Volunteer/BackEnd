package project.volunteer.domain.sehedule.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.recruitment.application.RecruitmentService;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.scheduleParticipation.service.ScheduleParticipationDtoService;
import project.volunteer.domain.scheduleParticipation.service.ScheduleParticipationService;
import project.volunteer.domain.sehedule.application.ScheduleService;
import project.volunteer.domain.sehedule.application.dto.ScheduleDetails;
import project.volunteer.domain.sehedule.application.dto.ScheduleParam;
import project.volunteer.domain.sehedule.domain.Schedule;
import project.volunteer.domain.user.application.UserService;
import project.volunteer.domain.user.domain.User;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleFacade {
    private final UserService userService;
    private final RecruitmentService recruitmentService;
    private final ScheduleService scheduleService;
    private final ScheduleParticipationService scheduleParticipationService;
    private final ScheduleParticipationDtoService scheduleParticipationDtoService;

    @Transactional
    public Long registerVolunteerPostSchedule(Long recruitmentNo, ScheduleParam param){
        Recruitment recruitment = recruitmentService.findPublishedRecruitment(recruitmentNo);

        return scheduleService.addSchedule(recruitment, param).getScheduleNo();
    }

    @Transactional
    public Long editVolunteerPostSchedule(Long recruitmentNo, Long scheduleNo, ScheduleParam param){
        Recruitment recruitment = recruitmentService.findPublishedRecruitment(recruitmentNo);

        return scheduleService.editSchedule(scheduleNo, recruitment, param).getScheduleNo();
    }

    @Transactional
    public void deleteVolunteerPostSchedule(Long recruitmentNo, Long scheduleNo){
        recruitmentService.findPublishedRecruitment(recruitmentNo);

        scheduleService.deleteSchedule(scheduleNo);

        scheduleParticipationService.deleteScheduleParticipation(scheduleNo);
    }

    public List<Schedule> findVolunteerPostCalendarSchedules(Long recruitmentNo, LocalDate startDay, LocalDate endDay){
        Recruitment recruitment = recruitmentService.findPublishedRecruitment(recruitmentNo);

        return scheduleService.findCalendarSchedules(recruitment, startDay, endDay);
    }

    public ScheduleDetails findVolunteerPostCalendarSchedule(Long userNo, Long recruitmentNo, Long scheduleNo){
        User user = userService.findUser(userNo);
        recruitmentService.findPublishedRecruitment(recruitmentNo);

        Schedule calendarSchedule = scheduleService.findCalendarSchedule(scheduleNo);
        String scheduleParticipationState = scheduleParticipationDtoService.findScheduleParticipationState(calendarSchedule, user);

        return ScheduleDetails.createScheduleDetails(calendarSchedule, scheduleParticipationState);
    }

    public ScheduleDetails findClosestVolunteerPostSchedule(Long recruitmentNo, Long userNo){
        User user = userService.findUser(userNo);
        Recruitment recruitment = recruitmentService.findPublishedRecruitment(recruitmentNo);

        Schedule closestSchedule = scheduleService.findClosestSchedule(recruitment.getRecruitmentNo());
        //TODO: 퍼사드 메서드에 로직이 들어가는게 좋은건가...움..
        //TODO: 가장 가까운 스케줄이 없으면 NULL을 리텅해야하긴 하는데..
        if(closestSchedule == null){
            return null;
        }

        String scheduleParticipationState = scheduleParticipationDtoService.findScheduleParticipationState(closestSchedule, user);

        return ScheduleDetails.createScheduleDetails(closestSchedule, scheduleParticipationState);
    }
}