package project.volunteer.domain.recruitment.domain;

import project.volunteer.global.common.converter.CodeCommonType;

public enum VolunteerType implements CodeCommonType {

    ALL("1", "모두"), ADULT("2", "성인"), TEENAGER("3", "청소년");

    private String code;
    private String viewName;
    VolunteerType(String code, String label) {
        this.code = code;
        this.viewName = label;
    }

    @Override
    public String getId() {
        return this.code;
    }

    @Override
    public String getDesc() {
        return this.viewName;
    }
}
