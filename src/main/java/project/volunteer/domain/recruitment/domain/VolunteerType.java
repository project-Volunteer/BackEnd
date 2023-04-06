package project.volunteer.domain.recruitment.domain;

import lombok.Getter;
import project.volunteer.global.common.converter.LegacyCodeCommonType;

public enum VolunteerType implements LegacyCodeCommonType {

    ALL("1", "모두"), ADULT("2", "성인"), TEENAGER("3", "청소년");

    private String code;
    private String viewName;
    VolunteerType(String code, String label) {
        this.code = code;
        this.viewName = label;
    }

    @Override
    public String getLegacyCode() {
        return this.code;
    }

    @Override
    public String getDesc() {
        return this.viewName;
    }
}
