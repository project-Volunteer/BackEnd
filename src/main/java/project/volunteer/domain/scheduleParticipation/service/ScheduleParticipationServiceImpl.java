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
import project.volunteer.global.common.component.State;
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
        //일정 조회(종료 일자 검증 포함)
        Schedule findSchedule = scheduleRepository.findActivateSchedule(scheduleNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_SCHEDULE,
                        String.format("Schedule to participant = [%d]", scheduleNo)));

        //일정 참여 남은 인원 검증
        Integer activeNum = scheduleParticipationRepository.countActiveParticipant(scheduleNo);
        if(activeNum >= findSchedule.getVolunteerNum()){
            throw new BusinessException(ErrorCode.INSUFFICIENT_CAPACITY,
                    String.format("ScheduleNo = [%d], Active participant num = [%d]", scheduleNo, activeNum));
        }

        //사용자 일정 참여 가능 상태 검증
        scheduleParticipationRepository.findByUserNoAndScheduleNo(loginUserNo, scheduleNo)
                .ifPresentOrElse(
                        sp -> {
                            if (sp.getState().equals(State.PARTICIPATING)) {
                                throw new BusinessException(ErrorCode.DUPLICATE_PARTICIPATION,
                                        String.format("ScheduleNo = [%d], UserNo = [%d], State = [%s]", scheduleNo, loginUserNo, sp.getState().name()));
                            }
                            //재신청
                            sp.participating();
                        },
                        () ->{
                            //신규 신청
                            //값이 있겠지만, get 으로 가져오는 게 맞을까? 기본적으로 예외를 처리해야하는게 좋은 설계 일까?
                            Participant findParticipant =
                                    participantRepository.findByRecruitment_RecruitmentNoAndParticipant_UserNo(recruitmentNo, loginUserNo)
                                            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_PARTICIPATION,
                                                    String.format("ScheduleNo = [%d], UserNo = [%d]", scheduleNo, loginUserNo)));

                            ScheduleParticipation createSP = ScheduleParticipation.createScheduleParticipation(findSchedule, findParticipant, State.PARTICIPATING);
                            scheduleParticipationRepository.save(createSP);
                        }
                );
    }

    @Override
    @Transactional
    public void cancel(Long scheduleNo, Long loginUserNo) {
        //일정 조회(종료 일자 검증 포함)
        scheduleRepository.findActivateSchedule(scheduleNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_SCHEDULE,
                        String.format("Schedule to cancel participant = [%d]", scheduleNo)));

        //일정 신청 상태인지 검증
        ScheduleParticipation findSp = scheduleParticipationRepository.findByUserNoAndScheduleNoAndState(loginUserNo, scheduleNo, State.PARTICIPATING)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_STATE,
                        String.format("UserNo = [%d], ScheduleNo = [%d]", loginUserNo, scheduleNo)));

        //일정 신청 취소 요청
        findSp.cancelParticipation();
    }

    @Override
    @Transactional
    public void approvalCancellation(Long scheduleNo, Long spNo) {
        //일정 조회(종료 일자 검증 포함)
        scheduleRepository.findActivateSchedule(scheduleNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_SCHEDULE,
                        String.format("Schedule to cancel approval = [%d]", scheduleNo)));

        //일정 취소 요청 상태인지 검증
        ScheduleParticipation findSp = scheduleParticipationRepository.findByScheduleParticipationNoAndState(spNo, State.PARTICIPATION_CANCEL)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_STATE,
                        String.format("ScheduleParticipationNo = [%d]", spNo)));

        //일정 취소 요청 승인
        findSp.cancelApproval();
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
                    if(!sp.getState().equals(State.PARTICIPATION_COMPLETE_UNAPPROVED)){
                        throw new BusinessException(ErrorCode.INVALID_STATE,
                                String.format("ScheduleParticipationNo = [%d], State = [%s]", sp.getScheduleParticipationNo(), sp.getState().name()));
                    }
                    //일정 참여 완료 승인
                    sp.completeApproval();
                });
    }

}
