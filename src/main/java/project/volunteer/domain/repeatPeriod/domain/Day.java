package project.volunteer.domain.repeatPeriod.domain;

import lombok.Data;
import lombok.Getter;

@Getter
public enum Day {

    MON("월요일"), TUES("화요일"), WED("수요일"), THRU("목요일"), FRI("금요일"), SAT("토요일"), SUN("일요일");

    private final String viewName;
    Day(String label) {
        this.viewName = label;
    }

    public String getViewName(){
        return viewName;
    }

    public static Day of(String value){
        for(Day day : Day.values()){
            if(day.name().equals(value.toUpperCase()))
                return day;
        }
        throw new IllegalArgumentException("일치하는 요일이 없습니다.");
    }
}
