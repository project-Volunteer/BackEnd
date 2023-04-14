package project.volunteer.global.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;

class DateUtilTest {

    @Test
    public void getSpecificWeekDay() {
        //given
        LocalDate date  = LocalDate.of(2023,4,1);

        //when
        LocalDate specificWeekDay = DateUtil.findSpecificWeekDay(date, 1, DayOfWeek.MONDAY);

        //then
        System.out.println("result = " + specificWeekDay);
    }

    @Test
    public void isExistWeekDay() {
        //give
        LocalDate date  = LocalDate.of(2023,4,3);

        //when
        Boolean existWeekDay = DateUtil.isExistWeekDay(date, 1);

        //then
        Assertions.assertThat(existWeekDay).isTrue();
    }

}