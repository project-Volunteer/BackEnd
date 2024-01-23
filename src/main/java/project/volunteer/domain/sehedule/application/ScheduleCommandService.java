package project.volunteer.domain.sehedule.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.recruitment.domain.Recruitment;
import project.volunteer.domain.scheduleParticipation.dao.ScheduleParticipationRepository;
import project.volunteer.domain.scheduleParticipation.domain.ScheduleParticipation;
import project.volunteer.domain.sehedule.application.dto.RegularScheduleCreateCommand;
import project.volunteer.domain.sehedule.application.timetableCreator.RepeatTimetableCreateProvider;
import project.volunteer.domain.sehedule.application.timetableCreator.RepeatTimetableCreator;
import project.volunteer.domain.sehedule.repository.ScheduleRepository;
import project.volunteer.domain.sehedule.domain.Schedule;
import project.volunteer.domain.sehedule.application.dto.ScheduleUpsertCommand;
import project.volunteer.global.common.component.ParticipantState;
import project.volunteer.global.common.component.Timetable;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ScheduleCommandService implements ScheduleCommandUseCase {
    private final ScheduleRepository scheduleRepository;
    private final RepeatTimetableCreateProvider repeatTimetableCreateProvider;

    private final ScheduleParticipationRepository scheduleParticipationRepository;

    @Override
    public Long addSchedule(final Recruitment recruitment, final ScheduleUpsertCommand command) {
        final Schedule schedule = command.toDomain(recruitment);
        return scheduleRepository.save(schedule).getScheduleNo();
    }

    @Override
    public List<Long> addRegulaerSchedule(final Recruitment recruitment, final RegularScheduleCreateCommand command) {
        RepeatTimetableCreator timetableCreator = repeatTimetableCreateProvider.getCreator(
                command.getRepeatPeriod());
        final List<Timetable> scheduleTimetable = timetableCreator.create(command.getRecruitmentTimetable(),
                command.getRepeatPeriod());
        final List<Schedule> schedules = command.toDomains(scheduleTimetable, recruitment);

        return scheduleRepository.saveAll(schedules)
                .stream()
                .map(Schedule::getScheduleNo)
                .collect(Collectors.toList());
    }

    @Override
    public Long editSchedule(final Long scheduleNo, final Recruitment recruitment,
                             final ScheduleUpsertCommand command) {
        final Schedule findSchedule = validAndGetSchedule(scheduleNo);
        findSchedule.change(recruitment, command.getTimetable(), command.getContent(), command.getOrganizationName(),
                command.getAddress(), command.getMaxParticipationNum());

        return findSchedule.getScheduleNo();
    }

    @Override
    public void deleteSchedule(final Long scheduleNo) {
        Schedule findSchedule = validAndGetSchedule(scheduleNo);
        findSchedule.delete();
    }

    @Override
    public void deleteAllSchedule(Long recruitmentNo) {
        scheduleRepository.findByRecruitment_RecruitmentNo(recruitmentNo)
                .forEach(Schedule::delete);
    }


    //TODO: 단일 쿼리로 리펙토링 필요
    //TODO: 배치 스케줄링 메서드
    //TODO: 리팩토링 해서 ScheduleParticipationRepository와 의존관계 없애기
    @Override
    public void scheduleParticipantStateUpdateProcess() {
        //완료된 일정 찾기
        List<Schedule> completedSchedules = scheduleRepository.findCompletedSchedule();

        for (Schedule schedule : completedSchedules) {
            List<ScheduleParticipation> findSps = scheduleParticipationRepository.findBySchedule_ScheduleNoAndState(
                    schedule.getScheduleNo(), ParticipantState.PARTICIPATING);
            for (ScheduleParticipation sp : findSps) {
                //일정 참여 완료 미승인 상태로 업데이트
                sp.updateState(ParticipantState.PARTICIPATION_COMPLETE_UNAPPROVED);
            }
        }
    }

    //일정 유효성 검사
    private Schedule validAndGetSchedule(Long scheduleNo) {
        return scheduleRepository.findValidSchedule(scheduleNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_SCHEDULE,
                        String.format("Schedule No = [%d]", scheduleNo)));
    }

}
