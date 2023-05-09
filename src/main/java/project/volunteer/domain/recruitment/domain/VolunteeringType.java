package project.volunteer.domain.recruitment.domain;

import lombok.Getter;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

import java.util.Arrays;

@Getter
public enum VolunteeringType {

    REG("정기"), IRREG("비정기");

    private final String viewName;
    VolunteeringType(String label) {
        this.viewName = label;
    }

    public String getViewName() {
        return viewName;
    }

    public static VolunteeringType of(String value) {

        return Arrays.stream(VolunteeringType.values())
                .filter(v -> v.name().equals(value.toUpperCase()))
                .findAny()
                .orElseThrow(() -> new BusinessException(ErrorCode.UNKNOWN_ENUM_VALUE, String.format("VolunteeringType Value = [%s]", value)));
    }
}
