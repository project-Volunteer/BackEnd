package project.volunteer.domain.scheduleParticipation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.recruitmentParticipation.domain.RecruitmentParticipation;
import project.volunteer.domain.scheduleParticipation.repository.ScheduleParticipationRepository;
import project.volunteer.domain.scheduleParticipation.domain.ScheduleParticipation;
import project.volunteer.domain.sehedule.domain.Schedule;
import project.volunteer.global.common.component.ParticipantState;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ScheduleParticipationCommandService implements ScheduleParticipationCommandUseCase {
    private final ScheduleParticipationRepository scheduleParticipationRepository;

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

    private ScheduleParticipation findScheduleParticipation(final Schedule schedule,
                                                            final RecruitmentParticipation recruitmentParticipation) {
        return scheduleParticipationRepository.findByScheduleAndRecruitmentParticipation(schedule,
                        recruitmentParticipation)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_SCHEDULE_PARTICIPATION));
    }
















    @Override
    public void cancel(Schedule schedule, RecruitmentParticipation participant) {
        //일정 참여 중 상태인지 검증
        ScheduleParticipation findSp = scheduleParticipationRepository.findByScheduleAndRecruitmentParticipationAndState(
                        schedule,
                        participant, ParticipantState.PARTICIPATING)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_STATE,
                        String.format("UserNo = [%d], ScheduleNo = [%d]", participant.getUser().getUserNo(),
                                schedule.getScheduleNo())));

        //일정 신청 취소 요청
        findSp.changeState(ParticipantState.PARTICIPATION_CANCEL);
    }

    @Override
    public void approvalCancellation(Schedule schedule, Long spNo) {
        //일정 취소 요청 상태인지 검증
        ScheduleParticipation findSp = scheduleParticipationRepository.findByIdAndState(spNo,
                        ParticipantState.PARTICIPATION_CANCEL)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_STATE,
                        String.format("ScheduleParticipationNo = [%d]", spNo)));

        //일정 취소 요청 승인
        findSp.changeState(ParticipantState.PARTICIPATION_CANCEL_APPROVAL);

        //일정 참가자 수 감소
        schedule.decreaseParticipant();
    }

    @Override
    public void approvalCompletion(List<Long> spNo) {
        scheduleParticipationRepository.findByIdIn(spNo).stream()
                .forEach(sp -> {
                    //일정 참여 완료 미승인 상태가 아닌 경우
                    if (!sp.isEqualState(ParticipantState.PARTICIPATION_COMPLETE_UNAPPROVED)) {
                        throw new BusinessException(ErrorCode.INVALID_STATE,
                                String.format("ScheduleParticipationNo = [%d], State = [%s]",
                                        sp.getId(), sp.getState().name()));
                    }
                    //일정 참여 완료 승인
                    sp.changeState(ParticipantState.PARTICIPATION_COMPLETE_APPROVAL);
                });
    }

    @Override
    public void deleteScheduleParticipation(Long scheduleNo) {
        scheduleParticipationRepository.findBySchedule_ScheduleNo(scheduleNo)
                .forEach(sp -> {
                    sp.delete();
                    sp.removeScheduleAndParticipant();
                });
    }

    @Override
    public void deleteAllScheduleParticipation(Long recruitmentNo) {
        scheduleParticipationRepository.findByRecruitmentNo(recruitmentNo)
                .forEach(sp -> {
                    sp.delete();
                    sp.removeScheduleAndParticipant();
                });
    }
}
