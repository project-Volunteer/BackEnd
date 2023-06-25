package project.volunteer.global.common.component;

import project.volunteer.global.common.converter.LegacyCodeCommonType;

public enum ParticipantState implements LegacyCodeCommonType {

    JOIN_AVAILABLE("r1", "팀 신청 가능"),
    JOIN_REQUEST("r2", "팀 신청"),
    JOIN_APPROVAL("r3", "팀 신청 승인"),
    JOIN_CANCEL("r4", "팀 신청 취소"),
    QUIT("r5", "팀 탈퇴"),
    DEPORT("r6", "팀 강제 탈퇴"),
    //JOIN_CANCEL, QUIT,DEPORT  는 '팀 재신청' 가능.

    PARTICIPATING("s1", "일정 참여 중"),
    PARTICIPATION_CANCEL("s2", "일정 참여 취소 요청"),
    PARTICIPATION_CANCEL_APPROVAL("s3", "일정 참여 취소 요청 승인"),
    PARTICIPATION_COMPLETE_UNAPPROVED("s4", "일정 참여 완료 미승인"),
    PARTICIPATION_COMPLETE_APPROVAL("s5", "일정 참가 완료 승인"),
    PARTICIPATION_AVAILABLE("s6", "일정 참여 가능"),

    DELETED("e1", "삭제"),
    UNAVAILABLE("e2","신청 불가") //모집 기간 종료 및 인원 초과
    ,;

    private String code;
    private String desc;

    ParticipantState(String code, String desc) {
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
