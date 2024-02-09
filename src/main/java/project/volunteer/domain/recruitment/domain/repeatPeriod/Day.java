package project.volunteer.domain.recruitment.domain.repeatPeriod;

import java.util.Objects;
import lombok.Getter;
import project.volunteer.global.common.converter.CodeCommonType;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

import java.util.Arrays;

@Getter
public enum Day implements CodeCommonType {

    MON(1, "월요일"), TUES(2, "화요일"), WED(3, "수요일"), THRU(4, "목요일"),
    FRI(5,"금요일"), SAT(6,"토요일"), SUN(7,"일요일"), NONE(0, "");

    private final Integer value;
    private final String viewName;
    Day(Integer value, String label) {
        this.value = value;
        this.viewName = label;
    }

    public static Day of(String value){
        if(Objects.isNull(value)) {
            throw new IllegalArgumentException();
        }

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

}
