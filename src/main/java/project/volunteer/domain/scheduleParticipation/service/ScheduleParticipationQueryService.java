package project.volunteer.domain.scheduleParticipation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.scheduleParticipation.repository.ScheduleParticipationRepository;
import project.volunteer.domain.scheduleParticipation.service.dto.ActiveParticipantSearchResult;
import project.volunteer.domain.scheduleParticipation.service.dto.CancelledParticipantDetail;
import project.volunteer.domain.scheduleParticipation.service.dto.CancelledParticipantsSearchResult;
import project.volunteer.domain.scheduleParticipation.service.dto.ParsingCompleteSchedule;
import project.volunteer.domain.scheduleParticipation.service.dto.CompletedParticipantList;
import project.volunteer.domain.scheduleParticipation.service.dto.ActiveParticipantDetail;
import project.volunteer.domain.sehedule.domain.Schedule;
import project.volunteer.global.common.component.ParticipantState;
import project.volunteer.global.common.dto.StateResult;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ScheduleParticipationQueryService implements ScheduleParticipationQueryUseCase {
    private final ScheduleParticipationRepository scheduleParticipationRepository;

    @Override
    public ActiveParticipantSearchResult searchActiveParticipationList(final Long scheduleNo) {
        final List<ParticipantState> states = List.of(ParticipantState.PARTICIPATING);
        final List<ActiveParticipantDetail> activeParticipantDetails = scheduleParticipationRepository.findScheduleParticipationDetailBy(
                        scheduleNo, states)
                .stream()
                .map(ActiveParticipantDetail::from)
                .collect(Collectors.toList());

        return new ActiveParticipantSearchResult(activeParticipantDetails);
    }

    @Override
    public CancelledParticipantsSearchResult searchCancelledParticipationList(final Long scheduleNo) {
        final List<ParticipantState> states = List.of(ParticipantState.PARTICIPATION_CANCEL);
        final List<CancelledParticipantDetail> cancelledParticipantDetails = scheduleParticipationRepository.findScheduleParticipationDetailBy(
                        scheduleNo, states)
                .stream()
                .map(CancelledParticipantDetail::from)
                .collect(Collectors.toList());

        return new CancelledParticipantsSearchResult(cancelledParticipantDetails);
    }









    @Override
    public List<CompletedParticipantList> findCompletedParticipants(Schedule schedule) {
        return scheduleParticipationRepository.findOptimizationParticipantByScheduleAndState(schedule.getScheduleNo(),
                        List.of(ParticipantState.PARTICIPATION_COMPLETE_APPROVAL,
                                ParticipantState.PARTICIPATION_COMPLETE_UNAPPROVED)).stream()
                .map(sp -> {
                    StateResult state = sp.isEqualParticipantState(ParticipantState.PARTICIPATION_COMPLETE_APPROVAL)
                            ? (StateResult.COMPLETE_APPROVED) : (StateResult.COMPLETE_UNAPPROVED);
                    return new CompletedParticipantList(sp.getScheduleParticipationNo(), sp.getNickname(),
                            sp.getEmail(), sp.getProfile(), state.getId());
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
