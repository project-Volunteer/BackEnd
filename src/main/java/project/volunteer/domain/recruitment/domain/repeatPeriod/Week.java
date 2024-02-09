package project.volunteer.domain.recruitment.domain.repeatPeriod;

import java.util.Objects;
import lombok.Getter;
import project.volunteer.global.common.converter.CodeCommonType;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

import java.util.Arrays;

@Getter
public enum Week implements CodeCommonType {

    FIRST(1,"첫째주"), SECOND(2,"둘째주"), THIRD(3, "셋째주"), FOUR(4, "넷째주"),
    FIVE(5,"다섯째주"), SIX(6,"여섯째주"), NONE(0, "");

    private final Integer value;
    private final String viewName;
    Week(int value, String viewName) {
        this.value = value;
        this.viewName = viewName;
    }

    public static Week of(String value){
        if(Objects.isNull(value)) {
            throw new IllegalArgumentException();
        }

        return Arrays.stream(Week.values())
                .filter(v -> v.getId().equals(value.toUpperCase()))
                .findAny()
                .orElseThrow(() -> new BusinessException(ErrorCode.UNKNOWN_ENUM_VALUE, String.format("Week Value = [%d]", value)));
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
