package project.volunteer.domain.scheduleParticipation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.scheduleParticipation.repository.ScheduleParticipationRepository;
import project.volunteer.domain.scheduleParticipation.service.dto.ActiveParticipantsSearchResult;
import project.volunteer.domain.scheduleParticipation.service.dto.CancelledParticipantDetail;
import project.volunteer.domain.scheduleParticipation.service.dto.CancelledParticipantsSearchResult;
import project.volunteer.domain.scheduleParticipation.service.dto.CompletedParticipantsSearchResult;
import project.volunteer.domain.scheduleParticipation.service.dto.ParsingCompleteSchedule;
import project.volunteer.domain.scheduleParticipation.service.dto.CompletedParticipantDetail;
import project.volunteer.domain.scheduleParticipation.service.dto.ActiveParticipantDetail;
import project.volunteer.global.common.component.ParticipantState;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ScheduleParticipationQueryService implements ScheduleParticipationQueryUseCase {
    private final ScheduleParticipationRepository scheduleParticipationRepository;

    @Override
    public ActiveParticipantsSearchResult searchActiveParticipationList(final Long scheduleNo) {
        final List<ParticipantState> states = List.of(ParticipantState.PARTICIPATING);
        final List<ActiveParticipantDetail> activeParticipantDetails = scheduleParticipationRepository.findScheduleParticipationDetailBy(
                        scheduleNo, states)
                .stream()
                .map(ActiveParticipantDetail::from)
                .collect(Collectors.toList());

        return new ActiveParticipantsSearchResult(activeParticipantDetails);
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
    public CompletedParticipantsSearchResult searchCompletedParticipationList(final Long scheduleNo) {
        final List<ParticipantState> states = ParticipantState.getParticipationCompletionState();
        final List<CompletedParticipantDetail> completedParticipantDetails = scheduleParticipationRepository.findScheduleParticipationDetailBy(
                        scheduleNo, states)
                .stream()
                .map(CompletedParticipantDetail::from)
                .collect(Collectors.toList());

        return new CompletedParticipantsSearchResult(completedParticipantDetails);
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
