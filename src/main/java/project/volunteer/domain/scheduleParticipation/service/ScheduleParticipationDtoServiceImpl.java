package project.volunteer.domain.scheduleParticipation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.scheduleParticipation.dao.ScheduleParticipationRepository;
import project.volunteer.domain.scheduleParticipation.service.dto.CancelledParticipantList;
import project.volunteer.domain.scheduleParticipation.service.dto.ParsingCompleteSchedule;
import project.volunteer.domain.scheduleParticipation.service.dto.CompletedParticipantList;
import project.volunteer.domain.scheduleParticipation.service.dto.ParticipatingParticipantList;
import project.volunteer.domain.sehedule.dao.ScheduleRepository;
import project.volunteer.global.common.component.ParticipantState;
import project.volunteer.global.common.dto.StateResponse;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ScheduleParticipationDtoServiceImpl implements ScheduleParticipationDtoService{

    private final ScheduleRepository scheduleRepository;
    private final ScheduleParticipationRepository scheduleParticipationRepository;

    @Override
    public List<ParticipatingParticipantList> findParticipatingParticipants(Long scheduleNo) {
        //일정 조회(삭제되지만 않은)
        scheduleRepository.findValidSchedule(scheduleNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_SCHEDULE,
                        String.format("ScheduleNo = [%d]", scheduleNo)));

        return scheduleParticipationRepository.findParticipantsByOptimization(scheduleNo, List.of(ParticipantState.PARTICIPATING)).stream()
                .map(p -> new ParticipatingParticipantList(p.getNickname(), p.getEmail(), p.getProfile()))
                .collect(Collectors.toList());
    }

    @Override
    public List<CancelledParticipantList> findCancelledParticipants(Long scheduleNo) {
        //일정 조회(삭제되지만 않은)
        scheduleRepository.findValidSchedule(scheduleNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_SCHEDULE,
                        String.format("ScheduleNo = [%d]", scheduleNo)));

        return scheduleParticipationRepository.findParticipantsByOptimization(scheduleNo, List.of(ParticipantState.PARTICIPATION_CANCEL)).stream()
                .map(p -> new CancelledParticipantList(p.getUserNo(), p.getNickname(), p.getEmail(), p.getProfile()))
                .collect(Collectors.toList());
    }

    @Override
    public List<CompletedParticipantList> findCompletedParticipants(Long scheduleNo) {
        //일정 조회(삭제되지만 않은)
        scheduleRepository.findValidSchedule(scheduleNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_SCHEDULE,
                        String.format("ScheduleNo = [%d]", scheduleNo)));

        return scheduleParticipationRepository.findParticipantsByOptimization(scheduleNo,
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
