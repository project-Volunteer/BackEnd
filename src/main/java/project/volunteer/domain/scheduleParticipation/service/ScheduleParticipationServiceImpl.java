package project.volunteer.domain.scheduleParticipation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.participation.dao.ParticipantRepository;
import project.volunteer.domain.participation.domain.Participant;
import project.volunteer.domain.scheduleParticipation.dao.ScheduleParticipationRepository;
import project.volunteer.domain.scheduleParticipation.domain.ScheduleParticipation;
import project.volunteer.domain.sehedule.dao.ScheduleRepository;
import project.volunteer.domain.sehedule.domain.Schedule;
import project.volunteer.global.common.component.ParticipantState;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ScheduleParticipationServiceImpl implements ScheduleParticipationService {

    private final ScheduleRepository scheduleRepository;
    private final ParticipantRepository participantRepository;
    private final ScheduleParticipationRepository scheduleParticipationRepository;

    @Override
    @Transactional
    public void participate(Long recruitmentNo, Long scheduleNo, Long loginUserNo) {
        //일정 검증(존재 여부, 모집 기간)
        Schedule findSchedule = isActiveScheduleWithPERSSIMITIC_WRITE_Lock(scheduleNo);

        //모집 인원 검증
        if(findSchedule.isFullParticipant()){
            throw new BusinessException(ErrorCode.INSUFFICIENT_CAPACITY,
                    String.format("ScheduleNo = [%d], Active participant num = [%d]", findSchedule.getScheduleNo(), findSchedule.getCurrentVolunteerNum()));
        }

        scheduleParticipationRepository.findByUserNoAndScheduleNo(loginUserNo, scheduleNo)
                .ifPresentOrElse(
                        sp -> {
                            //중복 신청 검증(일정 참여중, 일정 참여 취소 요청)
                            if(sp.isEqualState(ParticipantState.PARTICIPATING) || sp.isEqualState(ParticipantState.PARTICIPATION_CANCEL)){
                                throw new BusinessException(ErrorCode.DUPLICATE_PARTICIPATION,
                                        String.format("ScheduleNo = [%d], UserNo = [%d], State = [%s]", findSchedule.getScheduleNo(), loginUserNo, sp.getState().name()));
                            }

                            //재신청
                            sp.updateState(ParticipantState.PARTICIPATING);
                        },
                        () ->{
                            //신규 신청
                            Participant findParticipant =
                                    participantRepository.findByRecruitment_RecruitmentNoAndParticipant_UserNo(recruitmentNo, loginUserNo)
                                            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_PARTICIPATION,
                                                    String.format("ScheduleNo = [%d], UserNo = [%d]", scheduleNo, loginUserNo)));

                            ScheduleParticipation createSP =
                                    ScheduleParticipation.createScheduleParticipation(findSchedule, findParticipant, ParticipantState.PARTICIPATING);
                            scheduleParticipationRepository.save(createSP);
                        }
                );

        findSchedule.increaseParticipant();
    }

    @Override
    @Transactional
    public void cancel(Long scheduleNo, Long loginUserNo) {
        //일정 검증(존재 여부, 모집 기간)
        Schedule findSchedule = isActiveSchedule(scheduleNo);

        //일정 참여 중 상태인지 검증
        ScheduleParticipation findSp = scheduleParticipationRepository.findByUserNoAndScheduleNoAndState(loginUserNo, scheduleNo, ParticipantState.PARTICIPATING)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_STATE,
                        String.format("UserNo = [%d], ScheduleNo = [%d]", loginUserNo, scheduleNo)));

        //일정 신청 취소 요청
        findSp.updateState(ParticipantState.PARTICIPATION_CANCEL);
    }

    @Override
    @Transactional
    public void approvalCancellation(Long scheduleNo, Long spNo) {
        //일정 검증(존재 여부, 모집 기간)
        Schedule findSchedule = isActiveSchedule(scheduleNo);

        //일정 취소 요청 상태인지 검증
        ScheduleParticipation findSp = scheduleParticipationRepository.findByScheduleParticipationNoAndState(spNo, ParticipantState.PARTICIPATION_CANCEL)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_STATE,
                        String.format("ScheduleParticipationNo = [%d]", spNo)));

        //일정 취소 요청 승인
        findSp.updateState(ParticipantState.PARTICIPATION_CANCEL_APPROVAL);

        //일정 참가자 수 감소
        findSchedule.decreaseParticipant();
    }

    @Override
    @Transactional
    public void approvalCompletion(Long scheduleNo, List<Long> spNo) {
        //일정 조회(삭제되지만 않은)
        scheduleRepository.findValidSchedule(scheduleNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_SCHEDULE,
                        String.format("Schedule to completion approval = [%d]", scheduleNo)));

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
    private Schedule isActiveSchedule(Long scheduleNo){
        //일정 조회(삭제되지 않은지만 검증)
        Schedule findSchedule = scheduleRepository.findValidSchedule(scheduleNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_SCHEDULE,
                        String.format("Schedule to participant = [%d]", scheduleNo)));

        //일정 마감 일자 조회
        if(!findSchedule.isAvailableDate()){
            throw new BusinessException(ErrorCode.EXPIRED_PERIOD_SCHEDULE,
                    String.format("ScheduleNo = [%d], participation period = [%s]", findSchedule.getScheduleNo(), findSchedule.getScheduleTimeTable().getEndDay().toString()));
        }

        return findSchedule;
    }
    private Schedule isActiveScheduleWithPERSSIMITIC_WRITE_Lock(Long scheduleNo){
        //일정 조회(삭제되지 않은지만 검증)
        Schedule findSchedule = scheduleRepository.findValidScheduleWithPESSIMISTIC_WRITE_Lock(scheduleNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_SCHEDULE,
                        String.format("Schedule to participant = [%d]", scheduleNo)));

        //일정 마감 일자 조회
        if(!findSchedule.isAvailableDate()){
            throw new BusinessException(ErrorCode.EXPIRED_PERIOD_SCHEDULE,
                    String.format("ScheduleNo = [%d], participation period = [%s]", findSchedule.getScheduleNo(), findSchedule.getScheduleTimeTable().getEndDay().toString()));
        }

        return findSchedule;
    }

}
