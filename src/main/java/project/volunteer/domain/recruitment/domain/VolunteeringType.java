package project.volunteer.domain.recruitment.domain;

public enum VolunteeringType {

    LONG("장기"), SHORT("단기");

    private final String viewName;
    VolunteeringType(String label) {
        this.viewName = label;
    }

    public String getViewName() {
        return viewName;
    }

    public static VolunteeringType of(String value) {

        for(VolunteeringType type : VolunteeringType.values()){
            if(type.name().equals(value.toUpperCase())){
                return type;
            }
        }
        throw new IllegalArgumentException("일치하는 봉사 유형이 없습니다");
    }
}
