package project.volunteer.domain.user.domain;

import lombok.Getter;

@Getter
public enum Gender {

    M(1, "남성"), W(0, "여성");

    private final int code;
    private final String viewName;

    Gender(int code, String viewName) {
        this.code = code;
        this.viewName = viewName;
    }
}
