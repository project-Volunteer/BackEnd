package project.volunteer.domain.scheduleParticipation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.scheduleParticipation.dao.ScheduleParticipationRepository;
import project.volunteer.domain.scheduleParticipation.domain.ScheduleParticipation;
import project.volunteer.domain.scheduleParticipation.service.dto.CancelledParticipantList;
import project.volunteer.domain.scheduleParticipation.service.dto.ParsingCompleteSchedule;
import project.volunteer.domain.scheduleParticipation.service.dto.CompletedParticipantList;
import project.volunteer.domain.scheduleParticipation.service.dto.ParticipatingParticipantList;
import project.volunteer.domain.sehedule.domain.Schedule;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.ParticipantState;
import project.volunteer.global.common.dto.StateResponse;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ScheduleParticipationDtoServiceImpl implements ScheduleParticipationDtoService{
    private final ScheduleParticipationRepository scheduleParticipationRepository;

    @Override
    public String findScheduleParticipationState(Schedule schedule, User user) {
        //일정 신청 내역 조회
        Optional<ScheduleParticipation> findSp =
                scheduleParticipationRepository.findByUserNoAndScheduleNo(user.getUserNo(), schedule.getScheduleNo());

        //일정 참가 완료 미승인
        if(findSp.isPresent() && findSp.get().isEqualState(ParticipantState.PARTICIPATION_COMPLETE_UNAPPROVED)){
            return StateResponse.COMPLETE_UNAPPROVED.getId();
        }

        //일정 참가 완료 승인
        if(findSp.isPresent() && findSp.get().isEqualState(ParticipantState.PARTICIPATION_COMPLETE_APPROVAL)){
            return StateResponse.COMPLETE_APPROVED.getId();
        }

        //일정 참여 기간 만료
        if(!schedule.isAvailableDate()){
            return StateResponse.DONE.getId();
        }

        //참여 중
        if(findSp.isPresent() && findSp.get().isEqualState(ParticipantState.PARTICIPATING)){
            return StateResponse.PARTICIPATING.getId();
        }

        //취소 요청
        if(findSp.isPresent() && findSp.get().isEqualState(ParticipantState.PARTICIPATION_CANCEL)){
            return StateResponse.CANCELLING.getId();
        }

        //인원 초과
        if(schedule.isFullParticipant()){
            return StateResponse.FULL.getId();
        }

        //신청 가능
        return StateResponse.AVAILABLE.getId();
    }

    @Override
    public List<ParticipatingParticipantList> findParticipatingParticipants(Schedule schedule) {
        return scheduleParticipationRepository.findOptimizationParticipantByScheduleAndState(schedule.getScheduleNo(), List.of(ParticipantState.PARTICIPATING)).stream()
                .map(p -> new ParticipatingParticipantList(p.getNickname(), p.getEmail(), p.getProfile()))
                .collect(Collectors.toList());
    }

    @Override
    public List<CancelledParticipantList> findCancelledParticipants(Schedule schedule) {
        return scheduleParticipationRepository.findOptimizationParticipantByScheduleAndState(schedule.getScheduleNo(), List.of(ParticipantState.PARTICIPATION_CANCEL)).stream()
                .map(p -> new CancelledParticipantList(p.getUserNo(), p.getNickname(), p.getEmail(), p.getProfile()))
                .collect(Collectors.toList());
    }

    @Override
    public List<CompletedParticipantList> findCompletedParticipants(Schedule schedule) {
        return scheduleParticipationRepository.findOptimizationParticipantByScheduleAndState(schedule.getScheduleNo(),
                List.of(ParticipantState.PARTICIPATION_COMPLETE_APPROVAL, ParticipantState.PARTICIPATION_COMPLETE_UNAPPROVED)).stream()
                .map(sp -> {
                    StateResponse state = sp.isEqualParticipantState(ParticipantState.PARTICIPATION_COMPLETE_APPROVAL)?(StateResponse.COMPLETE_APPROVED):(StateResponse.COMPLETE_UNAPPROVED);
                    return new CompletedParticipantList(sp.getUserNo(), sp.getNickname(), sp.getEmail(), sp.getProfile(), state.getId());
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ParsingCompleteSchedule> findCompleteScheduleList(Long loginUserNo, ParticipantState state) {
    	return scheduleParticipationRepository.findCompletedSchedules(loginUserNo, state).stream()
    			.map(cs -> {
    				return new ParsingCompleteSchedule(cs.getScheduleNo()
                                                     , cs.getRecruitmentTitle()
                                                     , cs.getEndDay().format(DateTimeFormatter.ofPattern("MM-dd-yyyy")));
    			})
    			.collect(Collectors.toList());
    }
}
