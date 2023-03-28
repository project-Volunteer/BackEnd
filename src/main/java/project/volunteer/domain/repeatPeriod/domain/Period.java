package project.volunteer.domain.repeatPeriod.domain;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum Period {

    WEEK("매주"), MONTH("매월");

    private final String viewName;

    Period(String label) {
        this.viewName = label;
    }

    public String getViewName() {
        return viewName;
    }

    public static Period of(String value){

        return Arrays.stream(Period.values())
                .filter(v -> v.name().equals(value.toUpperCase()))
                .findAny()
                .orElseThrow(()-> new IllegalArgumentException(String.format("Not found match period=[%s]", value)));
    }

}
