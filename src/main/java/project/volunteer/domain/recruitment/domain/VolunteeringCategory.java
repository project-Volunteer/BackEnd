package project.volunteer.domain.recruitment.domain;

import lombok.Getter;

@Getter
public enum VolunteeringCategory {

    ADMINSTRATION_ASSISTANCE("001", "행정보조"), CULTURAL_EVENT("002","문화행사"), RESIDENTIAL_ENV("003", "주거환경"),
    HOMELESS_DOG("004", "유기견봉사"), FRAM_VILLAGE("005", "농촌봉사"), HEALTH_MEDICAL("006","보건의료"),
    EDUCATION("007","교육봉사"), DISASTER("008","재해/재난"), FOREIGN_COUNTRY("009","해외/국제"),
    ETC("999","기타");

    private final String code;
    private final String viewName;
    VolunteeringCategory(String code, String label) {
        this.code = code;
        this.viewName = label;
    }

    public static VolunteeringCategory ofCode(String code) {

        for(VolunteeringCategory category : VolunteeringCategory.values()){
            if(category.code.equals(code)){
                return category;
            }
        }
        throw new IllegalArgumentException("Not found mismatch Volunteering Category. code:"+code);
    }

}
