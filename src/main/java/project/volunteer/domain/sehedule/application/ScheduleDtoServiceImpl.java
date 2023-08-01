package project.volunteer.domain.sehedule.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.recruitment.dao.RecruitmentRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.scheduleParticipation.dao.ScheduleParticipationRepository;
import project.volunteer.domain.scheduleParticipation.domain.ScheduleParticipation;
import project.volunteer.domain.sehedule.application.dto.ScheduleDetails;
import project.volunteer.domain.sehedule.dao.ScheduleRepository;
import project.volunteer.domain.sehedule.domain.Schedule;
import project.volunteer.global.common.component.ParticipantState;
import project.volunteer.global.common.dto.StateResponse;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleDtoServiceImpl implements ScheduleDtoService{

    private final ScheduleRepository scheduleRepository;
    private final RecruitmentRepository recruitmentRepository;
    private final ScheduleParticipationRepository scheduleParticipationRepository;

    @Override
    public ScheduleDetails findClosestSchedule(Long recruitmentNo, Long loginUserNo) {

        //봉사 모집글 존재 검증
        Recruitment findRecruitment = recruitmentRepository.findPublishedByRecruitmentNo(recruitmentNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_RECRUITMENT, String.format("Recruitment No = [%d]", recruitmentNo)));

        //모집 중인 가장 가까운 봉사 스케줄 찾기
        //없는 경우 빈 response 응답
        Optional<Schedule> nearestSchedule = scheduleRepository.findNearestSchedule(findRecruitment.getRecruitmentNo());
        if(nearestSchedule.isEmpty()){
            return null;
        }

        //사용자 일정 참여 가능 상태 확인
        String state = converterParticipantState(nearestSchedule.get(), loginUserNo);

        //DTO 생성
        return ScheduleDetails.createScheduleDetails(nearestSchedule.get(), state);
    }

    @Override
    public ScheduleDetails findCalendarSchedule(Long recruitmentNo, Long scheduleNo, Long loginUserNo) {

        //봉사 모집글 존재 검증
        Recruitment findRecruitment = recruitmentRepository.findPublishedByRecruitmentNo(recruitmentNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_RECRUITMENT, String.format("Recruitment No = [%d]", recruitmentNo)));

        //일정 검증
        Schedule findSchedule = scheduleRepository.findValidSchedule(scheduleNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_SCHEDULE, String.format("Schedule No = [%d]", scheduleNo)));


        //사용자 일정 참여 가능 상태 확인
        String state = converterParticipantState(findSchedule, loginUserNo);

        //DTO 생성
        return ScheduleDetails.createScheduleDetails(findSchedule, state);
    }

    /**
     * L1 : 일정 완료 승인, 일정 완료 미승인
     * L2 : 일정 참여 기간 만료
     * L3 : 일정 참여 중, 일정 취소 요청
     * L4 : 일정 참여 가능 인원 초과
     * L5 : 일정 참여 가능, 일정 취소 승인
     */
    //TODO: 코드상 case 구문도 괜찮은 듯?
    private String converterParticipantState(Schedule schedule, Long loginUserNo){
        //일정 신청 내역 조회
        Optional<ScheduleParticipation> findSp =
                scheduleParticipationRepository.findByUserNoAndScheduleNo(loginUserNo, schedule.getScheduleNo());

        //일정 참가 완료 미승인
        if(findSp.isPresent() && findSp.get().isEqualState(ParticipantState.PARTICIPATION_COMPLETE_UNAPPROVED)){
            return StateResponse.COMPLETE_UNAPPROVED.name();
        }

        //일정 참가 완료 승인
        if(findSp.isPresent() && findSp.get().isEqualState(ParticipantState.PARTICIPATION_COMPLETE_APPROVAL)){
            return StateResponse.COMPLETE_APPROVED.name();
        }

        //일정 참여 기간 만료
        if(!schedule.isAvailableDate()){
            return StateResponse.DONE.name();
        }

        //참여 중
        if(findSp.isPresent() && findSp.get().isEqualState(ParticipantState.PARTICIPATING)){
            return StateResponse.PARTICIPATING.name();
        }

        //취소 요청
        if(findSp.isPresent() && findSp.get().isEqualState(ParticipantState.PARTICIPATION_CANCEL)){
            return StateResponse.CANCELLING.name();
        }

        //인원 초과
        if(schedule.isFullParticipant()){
            return StateResponse.FULL.name();
        }

        //신청 가능
        return StateResponse.AVAILABLE.name();
    }

}

