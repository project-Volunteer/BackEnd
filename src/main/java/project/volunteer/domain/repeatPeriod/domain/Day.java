package project.volunteer.domain.repeatPeriod.domain;

import project.volunteer.global.common.converter.CodeCommonType;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

import java.util.Arrays;

public enum Day implements CodeCommonType {

    MON(1, "월요일"), TUES(2, "화요일"), WED(3, "수요일"), THRU(4, "목요일"),
    FRI(5,"금요일"), SAT(6,"토요일"), SUN(7,"일요일");

    private final Integer value;
    private final String viewName;
    Day(Integer value, String label) {
        this.value = value;
        this.viewName = label;
    }

    public static Day of(String value){
        return Arrays.stream(Day.values())
                .filter(v -> v.getId().equals(value.toUpperCase()))
                .findAny()
                .orElseThrow(() -> new BusinessException(ErrorCode.UNKNOWN_ENUM_VALUE, String.format("Day Value = [%d]", value)));
    }

    @Override
    public String getId() {
        return this.name();
    }
    @Override
    public String getDesc() {
        return this.viewName;
    }
    public Integer getValue(){
        return this.value;
    }
}
