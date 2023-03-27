package project.volunteer.domain.recruitment.domain;

public enum VolunteerType {

    ALL("모두"), ADULT("성인"), TEENAGER("청소년");

    private final String viewName;
    VolunteerType(String label) {
        this.viewName = label;
    }

    public String getViewName() {
        return viewName;
    }

    public static VolunteerType of(String value){
        for(VolunteerType type : VolunteerType.values()) {
            if(type.name().equals(value.toUpperCase())){
                return type;
            }
        }
        throw new IllegalArgumentException("일치하는 봉사자 유형이 없습니다.");
    }
}