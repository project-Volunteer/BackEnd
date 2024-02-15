package project.volunteer.domain.scheduleParticipation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.recruitmentParticipation.domain.Participant;
import project.volunteer.domain.scheduleParticipation.repository.ScheduleParticipationRepository;
import project.volunteer.domain.scheduleParticipation.domain.ScheduleParticipation;
import project.volunteer.domain.sehedule.domain.Schedule;
import project.volunteer.global.common.component.ParticipantState;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ScheduleParticipationServiceImpl implements ScheduleParticipationService {
    private final ScheduleParticipationRepository scheduleParticipationRepository;

    @Override
    @Transactional
    public void participate(Schedule schedule, Participant participant) {
        //모집 인원 검증
        if(schedule.isFullParticipant()){
            throw new BusinessException(ErrorCode.INSUFFICIENT_CAPACITY,
                    String.format("ScheduleNo = [%d], Active participant num = [%d]", schedule.getScheduleNo(), schedule.getCurrentVolunteerNum()));
        }

        scheduleParticipationRepository.findByScheduleAndParticipant(schedule, participant)
                .ifPresentOrElse(
                        sp -> {
                            //중복 신청 검증(일정 참여중, 일정 참여 취소 요청)
                            if(sp.isEqualState(ParticipantState.PARTICIPATING) || sp.isEqualState(ParticipantState.PARTICIPATION_CANCEL)){
                                throw new BusinessException(ErrorCode.DUPLICATE_PARTICIPATION,
                                        String.format("ScheduleNo = [%d], UserNo = [%d], State = [%s]", schedule.getScheduleNo(), participant.getParticipant().getUserNo(), sp.getState().name()));
                            }

                            //재신청
                            sp.updateState(ParticipantState.PARTICIPATING);
                        },
                        () ->{
                            //신규 신청
                            ScheduleParticipation createSP =
                                    ScheduleParticipation.createScheduleParticipation(schedule, participant, ParticipantState.PARTICIPATING);
                            scheduleParticipationRepository.save(createSP);
                        }
                );
        //일정 참여 인원수 증가
        schedule.increaseParticipant();
    }

    @Override
    @Transactional
    public void cancel(Schedule schedule, Participant participant) {
        //일정 참여 중 상태인지 검증
        ScheduleParticipation findSp =   scheduleParticipationRepository.findByScheduleAndParticipantAndState(schedule, participant, ParticipantState.PARTICIPATING)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_STATE,
                        String.format("UserNo = [%d], ScheduleNo = [%d]", participant.getParticipant().getUserNo(), schedule.getScheduleNo())));

        //일정 신청 취소 요청
        findSp.updateState(ParticipantState.PARTICIPATION_CANCEL);
    }

    @Override
    @Transactional
    public void approvalCancellation(Schedule schedule, Long spNo) {
        //일정 취소 요청 상태인지 검증
        ScheduleParticipation findSp = scheduleParticipationRepository.findByScheduleParticipationNoAndState(spNo, ParticipantState.PARTICIPATION_CANCEL)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_STATE,
                        String.format("ScheduleParticipationNo = [%d]", spNo)));

        //일정 취소 요청 승인
        findSp.updateState(ParticipantState.PARTICIPATION_CANCEL_APPROVAL);

        //일정 참가자 수 감소
        schedule.decreaseParticipant();
    }

    @Override
    @Transactional
    public void approvalCompletion(List<Long> spNo) {
        scheduleParticipationRepository.findByScheduleParticipationNoIn(spNo).stream()
                .forEach(sp -> {
                    //일정 참여 완료 미승인 상태가 아닌 경우
                    if(!sp.isEqualState(ParticipantState.PARTICIPATION_COMPLETE_UNAPPROVED)){
                        throw new BusinessException(ErrorCode.INVALID_STATE,
                                String.format("ScheduleParticipationNo = [%d], State = [%s]", sp.getScheduleParticipationNo(), sp.getState().name()));
                    }
                    //일정 참여 완료 승인
                    sp.updateState(ParticipantState.PARTICIPATION_COMPLETE_APPROVAL);
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
