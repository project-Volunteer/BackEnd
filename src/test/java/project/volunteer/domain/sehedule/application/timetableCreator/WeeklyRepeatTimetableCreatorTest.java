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
import project.volunteer.global.common.component.HourFormat;
import project.volunteer.global.common.component.Timetable;

class WeeklyRepeatTimetableCreatorTest {
    private final RepeatTimetableCreator repeatTimetableCreator = new WeeklyRepeatTimetableCreator();

    @DisplayName("모집글 기간 동안 매주 일요일, 월요일에 해당하는 날짜들을 생성한다.")
    @Test
    void createTimetableWeekly() {
        //given
        final Timetable recruitmentTimetable = createTimetable(
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 29));
        final RepeatPeriodCreateCommand repeatPeriod = new RepeatPeriodCreateCommand(Period.WEEK, null,
                List.of(Day.SUN, Day.MON));

        //when
        List<Timetable> timetables = repeatTimetableCreator.create(recruitmentTimetable, repeatPeriod);

        //then
        assertThat(timetables).hasSize(9)
                .extracting("startDay")
                .containsExactlyInAnyOrder(
                        LocalDate.of(2024, 1, 1),
                        LocalDate.of(2024, 1, 7),
                        LocalDate.of(2024, 1, 8),
                        LocalDate.of(2024, 1, 14),
                        LocalDate.of(2024, 1, 15),
                        LocalDate.of(2024, 1, 21),
                        LocalDate.of(2024, 1, 22),
                        LocalDate.of(2024, 1, 28),
                        LocalDate.of(2024, 1, 29)
                );
    }

    @DisplayName("해가 넘어가는 모집글 기간 동안 매주 토요일, 수요일에 해당하는 날짜들을 생성한다.")
    @Test
    void createTimetableWeeklyByNextYearBoundary() {
        //given
        final Timetable recruitmentTimetable = createTimetable(
                LocalDate.of(2023, 12, 25),
                LocalDate.of(2024, 1, 8));
        final RepeatPeriodCreateCommand repeatPeriod = new RepeatPeriodCreateCommand(Period.WEEK, null,
                List.of(Day.WED, Day.SAT));

        //when
        List<Timetable> timetables = repeatTimetableCreator.create(recruitmentTimetable, repeatPeriod);

        //then
        assertThat(timetables).hasSize(4)
                .extracting("startDay")
                .containsExactlyInAnyOrder(
                        LocalDate.of(2023, 12, 27),
                        LocalDate.of(2023, 12, 30),
                        LocalDate.of(2024, 1, 3),
                        LocalDate.of(2024, 1, 6)
                );
    }

    private Timetable createTimetable(LocalDate fromDate, LocalDate toDate) {
        return new Timetable(fromDate, toDate, HourFormat.PM, LocalTime.now(), 10);
    }

}