package project.volunteer.domain.user.domain;

public enum Role {
	ADMIN("관리자"), USER("사용자");
	
    private final String viewName;

    Role(String label) {
        this.viewName = label;
    }

    public String getViewName() {
        return viewName;
    }
}
