package project.volunteer.domain.repeatPeriod.domain;

import project.volunteer.global.common.converter.CodeCommonType;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

import java.util.Arrays;

public enum Period implements CodeCommonType {

    WEEK("매주"), MONTH("매월");

    private final String viewName;

    Period(String label) {
        this.viewName = label;
    }

    public static Period of(String value){

        return Arrays.stream(Period.values())
                .filter(v -> v.getId().equals(value.toUpperCase()))
                .findAny()
                .orElseThrow(()-> new BusinessException(ErrorCode.UNKNOWN_ENUM_VALUE, String.format("Period Value = [%s]", value)));
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
