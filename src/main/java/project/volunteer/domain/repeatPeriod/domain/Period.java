package project.volunteer.domain.repeatPeriod.domain;

import lombok.Getter;

@Getter
public enum Period {

    WEEK("매주"), MONTH("매월");

    private final String viewName;

    Period(String label) {
        this.viewName = label;
    }

    public String getViewName() {
        return viewName;
    }
}
