package project.volunteer.domain.recruitment.domain;

import lombok.Getter;

@Getter
public enum VolunteeringCategory {

    ADMINSTRATION_ASSISTANCE("행정보조"), CULTURAL_EVENT("문화행사"), RESIDENTIAL_ENV("주거환경"), HOMELESS_DOG("유기견봉사"),
    FRAM_VILLAGE("농촌봉사"), HEALTH_MEDICAL("보건의료"), EDUCATION("교육봉사"), DISASTER("재해/재난"), FOREIGN_COUNTRY("해외/국제"),
    ETC("기타");

    private final String label;
    VolunteeringCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

}
