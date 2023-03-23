package project.volunteer.domain.recruitment.domain;

public enum VolunteeringType {

    LONG("장기"), SHORT("단기");

    private final String label;
    VolunteeringType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

}
