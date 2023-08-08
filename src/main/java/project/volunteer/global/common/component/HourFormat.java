package project.volunteer.global.common.component;

import lombok.Getter;
import project.volunteer.global.common.converter.CodeCommonType;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

import java.util.Arrays;

public enum HourFormat implements CodeCommonType {

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

    @Override
    public String getId() {
        return this.name();
    }
    @Override
    public String getDesc() {
        return this.viewName;
    }
}
