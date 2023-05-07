package project.volunteer.global.common.component;

import lombok.Getter;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

import java.util.Arrays;

@Getter
public enum HourFormat {

    AM("오전"), PM("오후");

    private final String viewName;

    HourFormat(String viewName){
        this.viewName = viewName;
    }

    public static HourFormat ofName(String value){
        return Arrays.stream(HourFormat.values())
                .filter(h -> h.name().equals(value.toUpperCase()))
                .findAny()
                .orElseThrow(() -> new BusinessException(ErrorCode.UNKNOWN_ENUM_VALUE, String.format("HourFormat Value = [%s]", value)));
    }
}
