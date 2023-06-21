package project.volunteer.global.common.component;

import project.volunteer.global.common.converter.LegacyCodeCommonType;

public enum State implements LegacyCodeCommonType {

    JOIN_REQUEST("1", "팀 신청"), JOIN_APPROVAL("2", "팀 승인"),
    JOIN_CANCEL("3", "팀 신청 취소"), QUIT("4", "팀 탈퇴"), DEPORT("100", "강제 탈퇴"),
    //JOIN_CANCEL, QUIT,DEPORT  는 '팀 재신청' 가능.

    PARTICIPATING("s1", "일정 참여 승인"),
    PARTICIPATION_CANCEL("s2", "일정 참여 취소 요청"),
    PARTICIPATION_CANCEL_APPROVAL("s3", "일정 참여 취소 요청 승인"),
    PARTICIPATION_COMPLETE_UNAPPROVED("s4", "일정 참여 완료 미승인"),
    PARTICIPATION_COMPLETE_APPROVAL("s5", "일정 참가 완료 승인"),

    DELETED("101", "삭제")
    ;

    private String code;
    private String desc;

    State(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @Override
    public String getLegacyCode() {
        return this.code;
    }

    @Override
    public String getDesc() {
        return this.desc;
    }
}
