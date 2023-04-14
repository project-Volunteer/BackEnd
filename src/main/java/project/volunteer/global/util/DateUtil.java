package project.volunteer.global.util;

import lombok.extern.slf4j.Slf4j;
import project.volunteer.domain.repeatPeriod.domain.Day;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;

@Slf4j
public class DateUtil {
    
    //시작 날짜 이후(같은) 가장 가까운 요일의 날짜 찾기
    public static LocalDate findNearestDayOfWeek(LocalDate date, Day day) {
        LocalDate nearestDate = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.of(day.getValue())));
        return nearestDate;
    }

    //일주일 증가
    public static LocalDate nextWeek(LocalDate date){
        return date.with(TemporalAdjusters.next(date.getDayOfWeek()));
    }

    //년,월,주차,요일에 해당 하는 날짜 찾기
    //ISO 8601 표준
    public static LocalDate findSpecificWeekDay(LocalDate date, int week, DayOfWeek dayOfWeek){
        LocalDate firstDayOfMonth = LocalDate.of(date.getYear(), date.getMonthValue(), 1);
        WeekFields weekFields = WeekFields.ISO;

        //해당 달의 첫 주의 첫날(월요일)의 날짜를 찾음
        LocalDate firstDayOfFirstWeek = firstDayOfMonth.with(weekFields.weekOfMonth(), 1)
                .with(weekFields.dayOfWeek(), 1);

        //찾으려는 주의 요일이 날짜를 계산
        LocalDate specificWeekDay = firstDayOfFirstWeek.plusWeeks(week - 1)
                .with(dayOfWeek);

        return specificWeekDay;
    }

    //특정 달에 해당 주차가 존재하는지 검증
    //ISO 8601 표준
    public static Boolean isExistWeekDay(LocalDate date, int week){
        LocalDate firstDayOfMonth = LocalDate.of(date.getYear(), date.getMonth().getValue(), 1);
        LocalDate lastDayOfMonth = date.with(TemporalAdjusters.lastDayOfMonth());

        WeekFields weekFields = WeekFields.ISO;
        int firstWeek = firstDayOfMonth.get(weekFields.weekOfWeekBasedYear());
        int lastWeek = lastDayOfMonth.get(weekFields.weekOfWeekBasedYear());

        //연도를 넘어가는 경우
        if(lastWeek < firstWeek){
            LocalDate lastDayOfPreYear = firstDayOfMonth.minusDays(1);
            int lastWeekOfPreYear = lastDayOfPreYear.get(weekFields.weekOfWeekBasedYear());
            firstWeek -=lastWeekOfPreYear;
        }
        int totalWeekDay = lastWeek - firstWeek +1;

        //달의 첫번째 일이 첫주차에 속하지 않거나 연도가 넘어가는 경우
        if(firstWeek==0 || firstDayOfMonth.getDayOfWeek().getValue() > DayOfWeek.THURSDAY.getValue())
            totalWeekDay-=1;
        //달의 마지막 일의 주차가 다음달의 첫주차인 경우
        if(lastDayOfMonth.getDayOfWeek().getValue() < DayOfWeek.THURSDAY.getValue())
            totalWeekDay-=1;

        return week <= totalWeekDay;
    }

    //현재 날짜가 시작 날짜 이후 또는 같은지 검증
    public static Boolean isAfter(LocalDate date, LocalDate start){
        return date.isAfter(start) || date.isEqual(start);
    }

    //현재 날짜가 마감 날짜 이전 또는 같은지 검증
    public static Boolean isBefore(LocalDate date, LocalDate end){
        return date.isBefore(end)||date.isEqual(end);
    }

}
