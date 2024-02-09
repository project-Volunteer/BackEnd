package project.volunteer.domain.sehedule.application.timetableCreator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import project.volunteer.domain.recruitment.application.dto.command.RepeatPeriodCreateCommand;
import project.volunteer.domain.recruitment.domain.repeatPeriod.Period;
import project.volunteer.global.common.component.Timetable;
import project.volunteer.global.util.DateUtil;

@Component
public class WeeklyRepeatTimetableCreator implements RepeatTimetableCreator {

    @Override
    public boolean isSupported(final RepeatPeriodCreateCommand repeatPeriod) {
        return repeatPeriod.getPeriod()
                .equals(Period.WEEK);
    }

    @Override
    public List<Timetable> create(final Timetable recruitmentTimetable, final RepeatPeriodCreateCommand repeatPeriod) {
        List<LocalDate> initScheduleStartDates = repeatPeriod.getDayOfWeeks().stream()
                .map(dayOfWeek -> DateUtil.findNearestDateByDayOfWeek(recruitmentTimetable.getStartDay(), dayOfWeek))
                .collect(Collectors.toList());

        List<Timetable> scheduleTimetables = new ArrayList<>();
        for (LocalDate initScheduleStartDate : initScheduleStartDates) {
            addScheduleTimetable(scheduleTimetables, recruitmentTimetable, initScheduleStartDate);
        }
        return scheduleTimetables;
    }

    private void addScheduleTimetable(final List<Timetable> scheduleTimetables, final Timetable recruitmentTimetable,
                                      final LocalDate initScheduleStartDate) {
        for (LocalDate scheduleDate = initScheduleStartDate;
             DateUtil.isBefore(scheduleDate, recruitmentTimetable.getEndDay());
             scheduleDate = DateUtil.nextWeek(scheduleDate)) {

            Timetable timetable = new Timetable(scheduleDate, scheduleDate,
                    recruitmentTimetable.getHourFormat(),
                    recruitmentTimetable.getStartTime(), recruitmentTimetable.getProgressTime());
            scheduleTimetables.add(timetable);
        }
    }

}
