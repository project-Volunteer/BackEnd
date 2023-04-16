package project.volunteer.global.common.component;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum HourFormat {

    AM("오전"), PM("오후");

    private final String viewName;

    HourFormat(String viewName){
        this.viewName = viewName;
    }

    public static HourFormat ofName(String name){
        return Arrays.stream(HourFormat.values())
                .filter(h -> h.name().equals(name.toUpperCase()))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Not found match HourFormat=[%s]",name)));
    }
}
