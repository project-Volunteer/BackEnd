package project.volunteer.domain.sehedule.application;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.scheduleParticipation.repository.ScheduleParticipationRepository;
import project.volunteer.domain.sehedule.application.dto.query.ScheduleCalendarSearchResult;
import project.volunteer.domain.sehedule.application.dto.query.ScheduleDetailSearchResult;
import project.volunteer.domain.sehedule.repository.ScheduleRepository;
import project.volunteer.domain.sehedule.domain.Schedule;
import project.volunteer.domain.sehedule.repository.dao.ScheduleDetail;
import project.volunteer.global.common.component.ParticipantState;
import project.volunteer.global.common.dto.StateResult;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ScheduleQueryService implements ScheduleQueryUseCase {
    private final ScheduleRepository scheduleRepository;
    private final ScheduleParticipationRepository scheduleParticipationRepository;
    private final Clock clock;

    @Override
    public List<ScheduleCalendarSearchResult> searchScheduleCalender(final Recruitment recruitment,
                                                                     final LocalDate startDay,
                                                                     final LocalDate endDay) {
        return scheduleRepository.findScheduleDateBy(recruitment, startDay, endDay);
    }

    @Override
    public ScheduleDetailSearchResult searchScheduleDetail(final Long userNo, final Long scheduleNo) {
        final ScheduleDetail scheduleDetail = scheduleRepository.findScheduleDetailBy(scheduleNo);
        final Optional<ParticipantState> state = scheduleParticipationRepository.findStateBy(userNo, scheduleNo);
        final StateResult stateResult = StateResult.getScheduleState(state,
                scheduleDetail.isDone(LocalDate.now(clock)),
                scheduleDetail.isFull());

        return ScheduleDetailSearchResult.of(scheduleDetail, stateResult);
    }

    @Override
    public ScheduleDetailSearchResult searchClosestScheduleDetail(final Long userNo, final Long recruitmentNo) {
        if (!scheduleRepository.existNearestSchedule(recruitmentNo, LocalDate.now(clock))) {
            return ScheduleDetailSearchResult.createEmpty();
        }

        final ScheduleDetail scheduleDetail = scheduleRepository.findNearestScheduleDetailBy(recruitmentNo, LocalDate.now(clock));
        final Optional<ParticipantState> state = scheduleParticipationRepository.findStateBy(userNo, scheduleDetail.getNo());
        final StateResult stateResult = StateResult.getScheduleState(state,
                scheduleDetail.isDone(LocalDate.now(clock)),
                scheduleDetail.isFull());

        return ScheduleDetailSearchResult.of(scheduleDetail, stateResult);
    }

    @Override
    public Schedule findScheduleInProgress(final Long scheduleNo) {
        final Schedule schedule = validAndGetNotDeletedSchedule(scheduleNo);
        schedule.checkDoneDate(LocalDate.now(clock));
        return schedule;
    }

    @Override
    public Schedule findActivitedSchedule(final Long scheduleNo) {
        return validAndGetNotDeletedSchedule(scheduleNo);
    }

    private Schedule validAndGetNotDeletedSchedule(final Long scheduleNo) {
        return scheduleRepository.findNotDeletedSchedule(scheduleNo)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.NOT_EXIST_SCHEDULE, String.format("ScheduleNo = [%d]", scheduleNo)));
    }

    @Override
    public Schedule findScheduleInProgressWithPERSSIMITIC_WRITE_LOCK(final Long scheduleNo) {
        final Schedule schedule = scheduleRepository.findNotDeletedScheduleByPERSSIMITIC_LOCK(scheduleNo)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.NOT_EXIST_SCHEDULE, String.format("ScheduleNo = [%d]", scheduleNo)));

        schedule.checkDoneDate(LocalDate.now(clock));
        return schedule;
    }

}
