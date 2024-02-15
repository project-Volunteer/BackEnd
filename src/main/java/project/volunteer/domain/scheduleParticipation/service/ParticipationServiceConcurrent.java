package project.volunteer.domain.scheduleParticipation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.recruitmentParticipation.repository.ParticipantRepository;
import project.volunteer.domain.recruitmentParticipation.domain.Participant;
import project.volunteer.domain.scheduleParticipation.repository.ScheduleParticipationRepository;
import project.volunteer.domain.scheduleParticipation.domain.ScheduleParticipation;
import project.volunteer.domain.sehedule.repository.ScheduleRepository;
import project.volunteer.domain.sehedule.domain.Schedule;
import project.volunteer.global.common.component.ParticipantState;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

/**
 * 동시성 테스트를 위한 Service
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ParticipationServiceConcurrent {

    private final ScheduleRepository scheduleRepository;
    private final ParticipantRepository participantRepository;
    private final ScheduleParticipationRepository scheduleParticipationRepository;

    @Transactional
    public void participateWithoutLock(Long recruitmentNo, Long scheduleNo, Long loginUserNo) {
        //일정 검증(존재 여부, 모집 기간)
        Schedule findSchedule = isActiveScheduleWithoutLock(scheduleNo);

        /**
         * - 별다른 추가 필드 없이 쿼리를 통해 참가자 인원 수 체크하는 방식
         * - 동시성 이슈가 발생(두 번의 갱신 분실 문제)
         */
        Integer activeParticipantNum = scheduleParticipationRepository.countActiveParticipant(scheduleNo);
        if(findSchedule.getVolunteerNum() == activeParticipantNum){
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
    }

    @Transactional
    public void participateWithOPTIMSTICLock(Long recruitmentNo, Long scheduleNo, Long loginUserNo) {
        //일정 검증(존재 여부, 모집 기간)
        Schedule findSchedule = isActiveScheduleWithOPTIMSTICLock(scheduleNo);

        if(findSchedule.isFullParticipant()) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_CAPACITY,
                    String.format("ScheduleNo = [%d], Active participant num = [%d]", findSchedule.getScheduleNo(), findSchedule.getCurrentVolunteerNum()));
        }

        scheduleParticipationRepository.findByUserNoAndScheduleNo(loginUserNo, scheduleNo)
                .ifPresentOrElse(
                        sp -> {
                            //중복 신청 검증(일정 참여중, 일정 참여 취소 요청)
                            if (sp.isEqualState(ParticipantState.PARTICIPATING) || sp.isEqualState(ParticipantState.PARTICIPATION_CANCEL)) {
                                throw new BusinessException(ErrorCode.DUPLICATE_PARTICIPATION,
                                        String.format("ScheduleNo = [%d], UserNo = [%d], State = [%s]", findSchedule.getScheduleNo(), loginUserNo, sp.getState().name()));
                            }
                            //재신청
                            sp.updateState(ParticipantState.PARTICIPATING);
                            },
                        () -> {
                            //신규 신청
                            Participant findParticipant =
                                    participantRepository.findByRecruitment_RecruitmentNoAndParticipant_UserNo(recruitmentNo, loginUserNo)
                                            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_PARTICIPATION,
                                                    String.format("ScheduleNo = [%d], UserNo = [%d]", scheduleNo, loginUserNo)));

                            ScheduleParticipation createSP =
                                    ScheduleParticipation.createScheduleParticipation(findSchedule, findParticipant, ParticipantState.PARTICIPATING);
                            scheduleParticipationRepository.save(createSP);
                        });

        findSchedule.increaseParticipant();
    }

    @Transactional
    public void participateWithPERSSIMITIC_WRITE_Lock(Long recruitmentNo, Long scheduleNo, Long loginUserNo) {
        //일정 검증(존재 여부, 모집 기간)
        Schedule findSchedule = isActiveScheduleWithPERSSIMITIC_WRITE_Lock(scheduleNo);

        if(findSchedule.isFullParticipant()){
            throw new BusinessException(ErrorCode.INSUFFICIENT_CAPACITY,
                    String.format("ScheduleNo = [%d], Active participant num = [%d]", findSchedule.getScheduleNo(), findSchedule.getCurrentVolunteerNum()));
        }

        scheduleParticipationRepository.findByUserNoAndScheduleNo(loginUserNo, scheduleNo)
                .ifPresentOrElse(
                        sp -> {
                            //중복 신청 검증(일정 참여중, 일정 참여 취소 요청)
                            if (sp.isEqualState(ParticipantState.PARTICIPATING) || sp.isEqualState(ParticipantState.PARTICIPATION_CANCEL)) {
                                throw new BusinessException(ErrorCode.DUPLICATE_PARTICIPATION,
                                        String.format("ScheduleNo = [%d], UserNo = [%d], State = [%s]", findSchedule.getScheduleNo(), loginUserNo, sp.getState().name()));
                            }
                            //재신청
                            sp.updateState(ParticipantState.PARTICIPATING);
                        },
                        () -> {
                            //신규 신청
                            Participant findParticipant =
                                    participantRepository.findByRecruitment_RecruitmentNoAndParticipant_UserNo(recruitmentNo, loginUserNo)
                                            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_PARTICIPATION,
                                                    String.format("ScheduleNo = [%d], UserNo = [%d]", scheduleNo, loginUserNo)));

                            ScheduleParticipation createSP =
                                    ScheduleParticipation.createScheduleParticipation(findSchedule, findParticipant, ParticipantState.PARTICIPATING);
                            scheduleParticipationRepository.save(createSP);
                        });

        findSchedule.increaseParticipant();
    }

    private Schedule isActiveScheduleWithoutLock(Long scheduleNo){
        //일정 조회(삭제되지 않은지만 검증)
        Schedule findSchedule = scheduleRepository.findNotDeletedSchedule(scheduleNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_SCHEDULE,
                        String.format("Schedule to participant = [%d]", scheduleNo)));

        //일정 마감 일자 조회
        if(!findSchedule.isAvailableDate()){
            throw new BusinessException(ErrorCode.EXPIRED_PERIOD_SCHEDULE,
                    String.format("ScheduleNo = [%d], participation period = [%s]", findSchedule.getScheduleNo(), findSchedule.getScheduleTimeTable().getEndDay().toString()));
        }

        return findSchedule;
    }
    private Schedule isActiveScheduleWithOPTIMSTICLock(Long scheduleNo){
        //일정 조회(삭제되지 않은지만 검증)
        Schedule findSchedule = scheduleRepository.findNotDeletedScheduleByOPTIMSTIC_LOCK(scheduleNo)
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
        Schedule findSchedule = scheduleRepository.findNotDeletedScheduleByPERSSIMITIC_LOCK(scheduleNo)
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
