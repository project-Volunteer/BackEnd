package project.volunteer.domain.repeatPeriod.domain;

import lombok.Getter;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

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
                .orElseThrow(()-> new BusinessException(ErrorCode.UNKNOWN_ENUM_VALUE, String.format("Period Value = [%s]", value)));
    }

}
