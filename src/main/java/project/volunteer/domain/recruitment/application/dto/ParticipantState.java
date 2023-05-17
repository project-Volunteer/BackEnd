package project.volunteer.domain.recruitment.application.dto;

public enum ParticipantState {

    AVAILABLE("신청 가능"), PENDING("승인 대기"), APPROVED("승인 완료"), DONE("모집 마감"), NOTLOGIN("비 로그인")
    ;

    private String des;
    ParticipantState(String dec) {
        this.des = dec;
    }
}
