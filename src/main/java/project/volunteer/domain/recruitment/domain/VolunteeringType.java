package project.volunteer.domain.recruitment.domain;

import project.volunteer.global.common.converter.CodeCommonType;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

import java.util.Arrays;

public enum VolunteeringType implements CodeCommonType {

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
                .filter(v -> v.getId().equals(value.toUpperCase()))
                .findAny()
                .orElseThrow(() -> new BusinessException(ErrorCode.UNKNOWN_ENUM_VALUE, String.format("VolunteeringType Value = [%s]", value)));
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
