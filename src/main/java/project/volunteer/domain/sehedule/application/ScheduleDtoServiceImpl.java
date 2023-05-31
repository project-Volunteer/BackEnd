package project.volunteer.domain.sehedule.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.participation.dao.ParticipantRepository;
import project.volunteer.domain.recruitment.dao.RecruitmentRepository;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.scheduleParticipation.dao.ScheduleParticipationRepository;
import project.volunteer.domain.scheduleParticipation.domain.ScheduleParticipation;
import project.volunteer.domain.sehedule.application.dto.ScheduleDetails;
import project.volunteer.domain.sehedule.dao.ScheduleRepository;
import project.volunteer.domain.sehedule.domain.Schedule;
import project.volunteer.global.common.component.State;
import project.volunteer.global.common.response.ParticipantState;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;
import project.volunteer.global.util.SecurityUtil;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleDtoServiceImpl implements ScheduleDtoService{

    private final ScheduleRepository scheduleRepository;
    private final RecruitmentRepository recruitmentRepository;
    private final ParticipantRepository participantRepository;
    private final ScheduleParticipationRepository scheduleParticipationRepository;

    @Override
    public ScheduleDetails findClosestSchedule(Long recruitmentNo, Long loginUserNo) {

        //봉사 모집글 존재 검증
        Recruitment findRecruitment = recruitmentRepository.findPublishedByRecruitmentNo(recruitmentNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_RECRUITMENT, String.format("Recruitment No = [%d]", recruitmentNo)));
        //봉사 팀원 검증
        isRecruitmentTeamMember(findRecruitment, loginUserNo);

        //모집 중인 가장 가까운 봉사 스케줄 찾기
        //없는 경우 빈 response 응답
        Optional<Schedule> nearestSchedule = scheduleRepository.findNearestSchedule(findRecruitment.getRecruitmentNo());
        if(nearestSchedule.isEmpty()){
            return null;
        }

        //현재 일정에 참여중인 인원수 확인
        Integer activeParticipantNum = scheduleParticipationRepository.countActiveParticipant(nearestSchedule.get().getScheduleNo());

        //사용자 일정 참여 가능 상태 확인
        String state = decideUserStateAboutSchedule(nearestSchedule.get(), loginUserNo, activeParticipantNum);

        //DTO 생성
        return ScheduleDetails.createScheduleDetails(nearestSchedule.get(), activeParticipantNum, state);
    }

    @Override
    public ScheduleDetails findCalendarSchedule(Long recruitmentNo, Long scheduleNo, Long loginUserNo) {

        //봉사 모집글 존재 검증
        Recruitment findRecruitment = recruitmentRepository.findPublishedByRecruitmentNo(recruitmentNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_RECRUITMENT, String.format("Recruitment No = [%d]", recruitmentNo)));
        //봉사 팀원 검증
        isRecruitmentTeamMember(findRecruitment, loginUserNo);

        //일정 검증
        Schedule findSchedule = scheduleRepository.findValidByScheduleNo(scheduleNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_SCHEDULE, String.format("Schedule No = [%d]", scheduleNo)));

        //현재 일정에 참여중인 인원수 확인
        Integer activeParticipantNum = scheduleParticipationRepository.countActiveParticipant(scheduleNo);

        //사용자 일정 참여 가능 상태 확인
        String state = decideUserStateAboutSchedule(findSchedule, loginUserNo, activeParticipantNum);

        //DTO 생성
        return ScheduleDetails.createScheduleDetails(findSchedule, activeParticipantNum, state);
    }


    private void isRecruitmentTeamMember(Recruitment recruitment, Long loginUserNo){
        if(!participantRepository.existRecruitmentTeamMember(recruitment.getRecruitmentNo(), loginUserNo)){
            throw new BusinessException(ErrorCode.FORBIDDEN_RECRUITMENT_TEAM,
                    String.format("RecruitmentNo = [%d], UserNo = [%d]", recruitment.getRecruitmentNo(), loginUserNo));
        }
    }

    /**
     * 마감 -> 인원 수 초과
     * 참여중 -> 이미 참여 중인 상태 => 인원 수 초과시 '마감' 상태가 아닌 '참여중' 상태가 되어야된다.
     * 참여 취소 요청 -> 참여중이지만 취소 요청한 상태 => 인원 수 초과시 '마감' 상태가 아닌 '취소 요청' 상태가 되어야된다.
     * 참여 가능 -> 첫 참가자 or 취소 요청 승인 참가자 => 인원 수 초과시 '마감' 상태가 되어야 된다.
     */
    private String decideUserStateAboutSchedule(Schedule schedule, Long loginUserNo, int activeParticipantNum){
        String status = null;
        Optional<ScheduleParticipation> findState =
                scheduleParticipationRepository.findByUserNoAndScheduleNo(loginUserNo, schedule.getScheduleNo());

        //일정 참여 가능 상태(첫 신청 참여자, 취소 요청 승인 참여자)
        if(findState.isEmpty() ||
                findState.get().getState().equals(State.PARTICIPATION_CANCEL_APPROVAL)){
            status = ParticipantState.AVAILABLE.name();
        }

        //일정 참여중 상태
        if(findState.isPresent() && findState.get().getState().equals(State.PARTICIPATION_APPROVAL)){
            return ParticipantState.PARTICIPATING.name();
        }

        //일정 참여 취소 요청 상태
        if(findState.isPresent() && findState.get().getState().equals(State.PARTICIPATION_CANCEL)){
            return ParticipantState.CANCELLING.name();
        }

        //일정 신청 마감 상태(참여 가능인원 초과)
        if(schedule.getVolunteerNum() == activeParticipantNum){
            return ParticipantState.DONE.name();
        }

        return status;
    }

}

