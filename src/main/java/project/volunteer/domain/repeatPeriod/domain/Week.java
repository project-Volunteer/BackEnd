package project.volunteer.domain.repeatPeriod.domain;

import lombok.Getter;

import java.util.Arrays;

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

        return Arrays.stream(Week.values())
                .filter(v -> v.name().equals(value.toUpperCase()))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Not found match week=[%s]",value)));
    }
}
