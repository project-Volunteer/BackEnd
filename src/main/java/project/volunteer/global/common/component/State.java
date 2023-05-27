package project.volunteer.global.common.component;

import project.volunteer.global.common.converter.LegacyCodeCommonType;

public enum State implements LegacyCodeCommonType {

    JOIN_REQUEST("1", "팀 신청"), JOIN_APPROVAL("2", "팀 승인"),
    JOIN_CANCEL("3", "팀 신청 취소"), QUIT("4", "팀 탈퇴"),
    //JOIN_CANCEL, QUIT,DEPORT  는 '팀 재신청' 가능.

    PARTICIPATION_REQUEST("5", "참여 신청"), PARTICIPATION_APPROVAL("6", "참여 승인"),
    PARTICIPATION_CANCEL("7", "참여 취소 요청"), PARTICIPATION_CANCEL_APPROVAL("8", "참여 취소 승인"),

    FINISH("99", "참가 완료"), DEPORT("100", "강제 탈퇴"), DELETED("101", "삭제")

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
