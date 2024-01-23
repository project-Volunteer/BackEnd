package project.volunteer.global.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import project.volunteer.domain.recruitment.domain.Day;
import project.volunteer.domain.recruitment.domain.Week;

class DateUtilTest {

    @DisplayName("년, 월, 주차, 요일에 해당하는 날짜를 찾는다.")
    @Test
    public void findSpecificDate() {
        //given
        final LocalDate expectedDate = LocalDate.of(2024, 1, 7);

        //when
        LocalDate specificDate = DateUtil.findSpecificDate(2024, 1, Week.FIRST, Day.SUN);

        //then
        assertThat(specificDate).isEqualTo(expectedDate);
    }

    @DisplayName("주차 경계에 해당하는 년, 월, 주차, 요일에 해당하는 날짜를 찾는다.")
    @Test
    public void findSpecificDateByWeekBoundary() {
        //given
        final LocalDate expectedDate = LocalDate.of(2024, 1, 29);

        //when
        LocalDate specificDate = DateUtil.findSpecificDate(2024, 2, Week.FIRST, Day.MON);

        //then
        assertThat(specificDate).isEqualTo(expectedDate);
    }

    @DisplayName("2024년 3월에는 5주차가 존재하지 않는다.")
    @Test
    public void notExistWeek() {
        //give
        LocalDate date = LocalDate.of(2024, 3, 1);

        //when
        Boolean existWeekDay = DateUtil.isExistWeek(date, Week.FIVE);

        //then
        assertThat(existWeekDay).isFalse();
    }

    @DisplayName("2024년 2월에는 5주차가 존재한다.")
    @Test
    public void existWeek() {
        //give
        LocalDate date = LocalDate.of(2024, 2, 1);

        //when
        Boolean existWeekDay = DateUtil.isExistWeek(date, Week.FIVE);

        //then
        assertThat(existWeekDay).isTrue();
    }

}