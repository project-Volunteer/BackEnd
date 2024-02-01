package project.volunteer.domain.sehedule.mapper;

import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.recruitment.application.RecruitmentService;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.scheduleParticipation.service.ScheduleParticipationDtoService;
import project.volunteer.domain.scheduleParticipation.service.ScheduleParticipationService;
import project.volunteer.domain.sehedule.application.ScheduleCommandUseCase;
import project.volunteer.domain.sehedule.application.ScheduleQueryUseCase;
import project.volunteer.domain.sehedule.application.dto.query.ScheduleDetailSearchResult;
import project.volunteer.domain.sehedule.application.dto.ScheduleUpsertCommand;
import project.volunteer.domain.sehedule.application.dto.query.ScheduleCalendarSearchResult;
import project.volunteer.domain.sehedule.domain.Schedule;
import project.volunteer.domain.user.application.UserService;
import project.volunteer.domain.user.domain.User;

import java.time.LocalDate;
import java.util.List;
import project.volunteer.global.common.component.ParticipantState;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleFacade {
    private final UserService userService;
    private final RecruitmentService recruitmentService;
    private final ScheduleCommandUseCase scheduleCommandService;
    private final ScheduleQueryUseCase scheduleQueryService;

    private final ScheduleParticipationService scheduleParticipationService;
    private final ScheduleParticipationDtoService scheduleParticipationDtoService;

    @Transactional
    public Long registerVolunteerPostSchedule(Long recruitmentNo, ScheduleUpsertCommand param) {
        Recruitment recruitment = recruitmentService.findPublishedRecruitment(recruitmentNo);

        return scheduleCommandService.addSchedule(recruitment, param);
    }

    @Transactional
    public Long editVolunteerPostSchedule(Long recruitmentNo, Long scheduleNo, ScheduleUpsertCommand param) {
        Recruitment recruitment = recruitmentService.findPublishedRecruitment(recruitmentNo);

        return scheduleCommandService.editSchedule(scheduleNo, recruitment, param);
    }

    @Transactional
    public void deleteVolunteerPostSchedule(Long recruitmentNo, Long scheduleNo) {
        recruitmentService.findPublishedRecruitment(recruitmentNo);

        scheduleCommandService.deleteSchedule(scheduleNo);

        scheduleParticipationService.deleteScheduleParticipation(scheduleNo);
    }

    public List<ScheduleCalendarSearchResult> findScheduleCalendar(Long recruitmentNo, LocalDate startDay,
                                                                   LocalDate endDay) {
        Recruitment recruitment = recruitmentService.findPublishedRecruitment(recruitmentNo);
        return scheduleQueryService.searchScheduleCalender(recruitment, startDay, endDay);
    }

    public ScheduleDetailSearchResult findScheduleDetail(Long userNo, Long scheduleNo) {
        ScheduleDetailSearchResult scheduleSearchResult = scheduleQueryService.searchScheduleDetail(scheduleNo);
        Optional<ParticipantState> participantState = scheduleParticipationDtoService.searchState(scheduleNo, userNo);
        scheduleSearchResult.setResponseState(participantState);

        return scheduleSearchResult;
    }

    public ScheduleDetailSearchResult findClosestScheduleDetail(Long userNo, Long recruitmentNo) {
        LocalDate currentDate = LocalDate.now();
        ScheduleDetailSearchResult scheduleSearchResult = scheduleQueryService.searchClosestScheduleDetail(
                recruitmentNo, currentDate);

        if (Objects.nonNull(scheduleSearchResult)) {
            Optional<ParticipantState> participantState = scheduleParticipationDtoService.searchState(
                    scheduleSearchResult.getNo(), userNo);
            scheduleSearchResult.setResponseState(participantState);
        }

        return scheduleSearchResult;
    }
}
