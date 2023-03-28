package project.volunteer.domain.recruitment.domain;

import java.util.Arrays;

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

        return Arrays.stream(VolunteerType.values())
                .filter(v -> v.name().equals(value.toUpperCase()))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Not found match volunteer type=[%s]", value)));
    }
}
