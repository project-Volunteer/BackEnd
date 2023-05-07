package project.volunteer.domain.repeatPeriod.domain;

import lombok.Data;
import lombok.Getter;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

import java.util.Arrays;

@Getter
public enum Day {

    MON(1, "월요일"), TUES(2, "화요일"), WED(3, "수요일"), THRU(4, "목요일"),
    FRI(5,"금요일"), SAT(6,"토요일"), SUN(7,"일요일");

    private final Integer value;
    private final String viewName;
    Day(Integer value, String label) {
        this.value = value;
        this.viewName = label;
    }

    public static Day ofValue(int value){
        return Arrays.stream(Day.values())
                .filter(v -> v.getValue().equals(value))
                .findAny()
                .orElseThrow(() -> new BusinessException(ErrorCode.UNKNOWN_ENUM_VALUE, String.format("Day Value = [%d]", value)));
    }
}
