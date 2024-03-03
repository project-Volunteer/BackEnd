package project.volunteer.domain.scheduleParticipation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.recruitmentParticipation.domain.RecruitmentParticipation;
import project.volunteer.domain.scheduleParticipation.domain.ScheduleParticipations;
import project.volunteer.domain.scheduleParticipation.repository.ScheduleParticipationRepository;
import project.volunteer.domain.scheduleParticipation.domain.ScheduleParticipation;
import project.volunteer.domain.sehedule.domain.Schedule;
import project.volunteer.domain.sehedule.repository.ScheduleRepository;
import project.volunteer.global.common.component.ParticipantState;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ScheduleParticipationCommandService implements ScheduleParticipationCommandUseCase {
    private final ScheduleParticipationRepository scheduleParticipationRepository;
    private final ScheduleRepository scheduleRepository;

    @Override
    public Long participate(final Schedule schedule, final RecruitmentParticipation recruitmentParticipation) {
        checkIsFull(schedule);

        if (!scheduleParticipationRepository.existsByScheduleAndRecruitmentParticipation(schedule,
                recruitmentParticipation)) {
            ScheduleParticipation newScheduleParticipation = new ScheduleParticipation(schedule,
                    recruitmentParticipation, ParticipantState.PARTICIPATING);
            schedule.increaseParticipationNum(1);
            return scheduleParticipationRepository.save(newScheduleParticipation).getId();
        }

        ScheduleParticipation scheduleParticipation = findScheduleParticipation(schedule, recruitmentParticipation);
        checkDuplicationParticipation(scheduleParticipation);
        scheduleParticipation.changeState(ParticipantState.PARTICIPATING);
        schedule.increaseParticipationNum(1);
        return scheduleParticipation.getId();
    }

    private void checkIsFull(final Schedule schedule) {
        if (schedule.isFull()) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_CAPACITY, schedule.toString());
        }
    }

    private void checkDuplicationParticipation(final ScheduleParticipation scheduleParticipation) {
        if (!scheduleParticipation.canReParticipation()) {
            throw new BusinessException(ErrorCode.DUPLICATE_SCHEDULE_PARTICIPATION, scheduleParticipation.toString());
        }
    }

    @Override
    public void cancelParticipation(final Schedule schedule, final RecruitmentParticipation recruitmentParticipation) {
        ScheduleParticipation scheduleParticipation = findScheduleParticipation(schedule, recruitmentParticipation);
        checkCancellationPossible(scheduleParticipation);
        scheduleParticipation.changeState(ParticipantState.PARTICIPATION_CANCEL);
    }

    private void checkCancellationPossible(final ScheduleParticipation scheduleParticipation) {
        if (!scheduleParticipation.canCancel()) {
            throw new BusinessException(ErrorCode.INVALID_STATE, scheduleParticipation.toString());
        }
    }

    private ScheduleParticipation findScheduleParticipation(final Schedule schedule,
                                                            final RecruitmentParticipation recruitmentParticipation) {
        return scheduleParticipationRepository.findByScheduleAndRecruitmentParticipation(schedule,
                        recruitmentParticipation)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_SCHEDULE_PARTICIPATION));
    }

    @Override
    public void approvalCancellation(final Schedule schedule, final List<Long> scheduleParticipationNos) {
        ScheduleParticipations scheduleParticipations = findScheduleParticipations(scheduleParticipationNos);
        scheduleParticipations.approveCancellations();

        schedule.decreaseParticipationNum(scheduleParticipations.getSize());
    }

    @Override
    public void approvalParticipationCompletion(final List<Long> scheduleParticipationNos) {
        ScheduleParticipations scheduleParticipations = findScheduleParticipations(scheduleParticipationNos);
        scheduleParticipations.approveCompletionist();
    }

    private ScheduleParticipations findScheduleParticipations(final List<Long> ids) {
        List<ScheduleParticipation> scheduleParticipations = scheduleParticipationRepository.findByIdIn(ids);

        if (ids.size() != scheduleParticipations.size()) {
            throw new BusinessException(ErrorCode.NOT_EXIST_SCHEDULE_PARTICIPATION);
        }
        return new ScheduleParticipations(scheduleParticipations);
    }

    @Override
    public void deleteAllScheduleParticipationBySchedule(final Long scheduleNo) {
        scheduleParticipationRepository.bulkUpdateDetachByScheduleNo(scheduleNo);
    }

    @Override
    public void deleteAllScheduleParticipationByRecruitment(final Long recruitmentNo) {
        List<Long> scheduleNos = scheduleRepository.findScheduleNosByRecruitmentNo(recruitmentNo);
        scheduleParticipationRepository.bulkUpdateDetachByScheduleNos(scheduleNos);
    }

}
