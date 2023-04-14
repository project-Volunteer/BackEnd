package project.volunteer.domain.repeatPeriod.domain;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum Week {

    FIRST(1,"첫째주"), SECOND(2,"둘째주"), THIRD(3, "셋째주"), FOUR(4, "넷째주"),
    FIVE(5,"다섯째주"), SIX(6,"여섯째주");

    private final Integer value;
    private final String viewName;
    Week(int value, String label) {
        this.value = value;
        this.viewName = label;
    }

    public static Week of(String value) {

        return Arrays.stream(Week.values())
                .filter(v -> v.name().equals(value.toUpperCase()))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Not found match week=[%s]",value)));
    }
}
