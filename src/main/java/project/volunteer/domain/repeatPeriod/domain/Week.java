package project.volunteer.domain.repeatPeriod.domain;

import lombok.Getter;

@Getter
public enum Week {

    FIRST("첫째주"), SECOND("둘째주"), THIRD("셋째주"), FOUR("넷째주"), FIVE("다섯째주"), SIX("여섯째주");

    private final String viewName;
    Week(String label) {
        this.viewName = label;
    }

    public String getViewName(){
        return viewName;
    }

    public static Week of(String value) {
        for(Week week : Week.values()){
            if(week.name().equals(value.toUpperCase()))
                return week;
        }
        throw new IllegalArgumentException("일치하는 주가 없습니다.");
    }
}
