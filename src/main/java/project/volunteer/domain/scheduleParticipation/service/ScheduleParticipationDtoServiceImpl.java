package project.volunteer.domain.scheduleParticipation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.scheduleParticipation.repository.ScheduleParticipationRepository;
import project.volunteer.domain.scheduleParticipation.service.dto.CancelledParticipantList;
import project.volunteer.domain.scheduleParticipation.service.dto.ParsingCompleteSchedule;
import project.volunteer.domain.scheduleParticipation.service.dto.CompletedParticipantList;
import project.volunteer.domain.scheduleParticipation.service.dto.ParticipatingParticipantList;
import project.volunteer.domain.sehedule.domain.Schedule;
import project.volunteer.global.common.component.ParticipantState;
import project.volunteer.global.common.dto.StateResult;

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
    public Optional<ParticipantState> searchState(Long scheduleNo, Long userNo) {
        return scheduleParticipationRepository.findStateBy(userNo, scheduleNo);
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
                .map(p -> new CancelledParticipantList(p.getScheduleParticipationNo(), p.getNickname(), p.getEmail(), p.getProfile()))
                .collect(Collectors.toList());
    }

    @Override
    public List<CompletedParticipantList> findCompletedParticipants(Schedule schedule) {
        return scheduleParticipationRepository.findOptimizationParticipantByScheduleAndState(schedule.getScheduleNo(),
                List.of(ParticipantState.PARTICIPATION_COMPLETE_APPROVAL, ParticipantState.PARTICIPATION_COMPLETE_UNAPPROVED)).stream()
                .map(sp -> {
                    StateResult state = sp.isEqualParticipantState(ParticipantState.PARTICIPATION_COMPLETE_APPROVAL)?(StateResult.COMPLETE_APPROVED):(StateResult.COMPLETE_UNAPPROVED);
                    return new CompletedParticipantList(sp.getScheduleParticipationNo(), sp.getNickname(), sp.getEmail(), sp.getProfile(), state.getId());
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
