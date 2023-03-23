package project.volunteer.domain.recruitment.domain;

public enum VolunteerType {

    ALL("모두"), ADULT("성인"), TEENAGER("청소년");

    private final String label;
    VolunteerType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
