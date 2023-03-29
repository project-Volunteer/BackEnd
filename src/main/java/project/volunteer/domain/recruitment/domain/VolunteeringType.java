package project.volunteer.domain.recruitment.domain;

import java.util.Arrays;

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

        return Arrays.stream(VolunteeringType.values())
                .filter(v -> v.name().equals(value.toUpperCase()))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Not found match volunteering type=[%s]", value)));
    }
}
