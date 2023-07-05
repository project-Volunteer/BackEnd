package project.volunteer.domain.scheduleParticipation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.scheduleParticipation.dao.ScheduleParticipationRepository;
import project.volunteer.domain.scheduleParticipation.service.dto.CancelledParticipantList;
import project.volunteer.domain.scheduleParticipation.service.dto.CompletedParticipantList;
import project.volunteer.domain.scheduleParticipation.service.dto.ParticipatingParticipantList;
import project.volunteer.domain.sehedule.dao.ScheduleRepository;
import project.volunteer.global.common.component.ParticipantState;
import project.volunteer.global.common.response.StateResponse;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

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
                    return new CompletedParticipantList(sp.getUserNo(), sp.getNickname(), sp.getEmail(), sp.getProfile(), state.name());
                })
                .collect(Collectors.toList());
    }
}
