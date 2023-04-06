package project.volunteer.global.common.component;

import project.volunteer.global.common.converter.LegacyCodeCommonType;

public enum State implements LegacyCodeCommonType {

    JOIN_REQUEST("1", "참가신청"), JOIN_APPROVAL("2", "참가승인"), CANCEL_REQUEST("3", "취소요청"), CANCEL_APPROVAL("4","취소승인"),
    FINISH("5", "참가완료"), DEPORT("6", "강제탈퇴");

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
