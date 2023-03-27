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

    public static Period of(String value){

        for(Period period : Period.values()){
            if(period.name().equals(value.toUpperCase())){
                return period;
            }
        }
        throw new IllegalArgumentException("일치하는 주기가 없습니다.");
    }

}
