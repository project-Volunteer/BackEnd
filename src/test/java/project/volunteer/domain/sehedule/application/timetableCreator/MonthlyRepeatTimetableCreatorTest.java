package project.volunteer.domain.sehedule.application.timetableCreator;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import project.volunteer.domain.recruitment.application.dto.command.RepeatPeriodCreateCommand;
import project.volunteer.domain.recruitment.domain.repeatPeriod.Day;
import project.volunteer.domain.recruitment.domain.repeatPeriod.Period;
import project.volunteer.domain.recruitment.domain.repeatPeriod.Week;
import project.volunteer.global.common.component.HourFormat;
import project.volunteer.global.common.component.Timetable;

class MonthlyRepeatTimetableCreatorTest {
    private final RepeatTimetableCreator repeatTimetableCreator = new MonthlyRepeatTimetableCreator();

    @DisplayName("모집글 기간 동안 첫째주 일요일, 월요일에 해당하는 날짜들을 생성한다.")
    @Test
    void createTimetableMonthly() {
        //given
        final Timetable recruitmentTimetable = createTimetable(
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 3, 29));
        final RepeatPeriodCreateCommand repeatPeriod = new RepeatPeriodCreateCommand(Period.WEEK, Week.FIRST,
                List.of(Day.SUN, Day.MON));

        //when
        List<Timetable> timetables = repeatTimetableCreator.create(recruitmentTimetable, repeatPeriod);

        //then
        assertThat(timetables).hasSize(6)
                .extracting("startDay")
                .containsExactlyInAnyOrder(
                        LocalDate.of(2024, 1, 1),
                        LocalDate.of(2024, 1, 7),
                        LocalDate.of(2024, 1, 29),
                        LocalDate.of(2024, 2, 4),
                        LocalDate.of(2024, 3, 4),
                        LocalDate.of(2024, 3, 10)
                );
    }

    @DisplayName("해가 넘어가는 모집글 기간 동안 둘째주 수요일, 목요일에 해당하는 날짜들을 생성한다.")
    @Test
    void createTimetableMonthlyByNextYearBoundary() {
        //given
        final Timetable recruitmentTimetable = createTimetable(
                LocalDate.of(2023, 12, 1),
                LocalDate.of(2024, 1, 31));
        final RepeatPeriodCreateCommand repeatPeriod = new RepeatPeriodCreateCommand(Period.WEEK, Week.SECOND,
                List.of(Day.WED, Day.THRU));

        //when
        List<Timetable> timetables = repeatTimetableCreator.create(recruitmentTimetable, repeatPeriod);

        //then
        assertThat(timetables).hasSize(4)
                .extracting("startDay")
                .containsExactlyInAnyOrder(
                        LocalDate.of(2023, 12, 13),
                        LocalDate.of(2023, 12, 14),
                        LocalDate.of(2024, 1, 10),
                        LocalDate.of(2024, 1, 11)
                );
    }

    private Timetable createTimetable(LocalDate fromDate, LocalDate toDate) {
        return new Timetable(fromDate, toDate, HourFormat.PM, LocalTime.now(), 10);
    }

}