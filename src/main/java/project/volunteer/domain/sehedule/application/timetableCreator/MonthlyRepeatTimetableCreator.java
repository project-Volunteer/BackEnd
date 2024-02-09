package project.volunteer.domain.sehedule.application.timetableCreator;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.stereotype.Component;
import project.volunteer.domain.recruitment.application.dto.command.RepeatPeriodCreateCommand;
import project.volunteer.domain.recruitment.domain.repeatPeriod.Period;
import project.volunteer.global.common.component.Timetable;
import project.volunteer.global.util.DateUtil;

@Component
public class MonthlyRepeatTimetableCreator implements RepeatTimetableCreator {
    private final static int MONTHS_IN_YEAR = 12;

    @Override
    public boolean isSupported(final RepeatPeriodCreateCommand repeatPeriod) {
        return repeatPeriod.getPeriod()
                .equals(Period.MONTH);
    }

    @Override
    public List<Timetable> create(final Timetable recruitmentTimetable, final RepeatPeriodCreateCommand repeatPeriod) {
        int totalMonthByEndDay =
                recruitmentTimetable.getEndDay().getYear() * MONTHS_IN_YEAR + recruitmentTimetable.getEndDay()
                        .getMonthValue();
        int totalMonthByStartDay =
                recruitmentTimetable.getStartDay().getYear() * MONTHS_IN_YEAR + recruitmentTimetable.getStartDay()
                        .getMonthValue();
        int diffMonth = totalMonthByEndDay - totalMonthByStartDay;

        return IntStream.rangeClosed(0, diffMonth)
                .mapToObj(month -> recruitmentTimetable.getStartDay().plusMonths(month))
                .filter(addedMonthDate -> DateUtil.isExistWeek(addedMonthDate, repeatPeriod.getWeek()))
                .map(addedMonthDate -> generateTimetableByWeekAndDayOfWeek(recruitmentTimetable, repeatPeriod,
                        addedMonthDate))
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    private List<Timetable> generateTimetableByWeekAndDayOfWeek(final Timetable recruitmentTimetable,
                                                                final RepeatPeriodCreateCommand repeatPeriod,
                                                                final LocalDate targetDate) {
        return repeatPeriod.getDayOfWeeks().stream()
                .map(dayOfWeek -> DateUtil.findSpecificDate(targetDate.getYear(), targetDate.getMonthValue(),
                        repeatPeriod.getWeek(), dayOfWeek))
                .filter(date -> DateUtil.isAfter(date, recruitmentTimetable.getStartDay()))
                .filter(date -> DateUtil.isBefore(date, recruitmentTimetable.getEndDay()))
                .map(date -> new Timetable(date, date,
                        recruitmentTimetable.getHourFormat(),
                        recruitmentTimetable.getStartTime(), recruitmentTimetable.getProgressTime()))
                .collect(Collectors.toList());
    }


}
