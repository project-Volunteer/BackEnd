package project.volunteer.global.common.component;

import java.util.List;
import project.volunteer.global.common.converter.CodeCommonType;

public enum ParticipantState implements CodeCommonType {

    JOIN_REQUEST("r1", "팀 신청"),
    JOIN_APPROVAL("r2", "팀 신청 승인"),
    JOIN_CANCEL("r3", "팀 신청 취소"),
    QUIT("r4", "팀 탈퇴"),
    DEPORT("r5", "팀 강제 탈퇴"),
    //JOIN_CANCEL, QUIT,DEPORT  는 '팀 재신청' 가능.

    PARTICIPATING("s1", "일정 참여 중"),
    PARTICIPATION_CANCEL("s2", "일정 참여 취소 요청"),
    PARTICIPATION_CANCEL_APPROVAL("s3", "일정 참여 취소 요청 승인"),
    PARTICIPATION_COMPLETE_UNAPPROVED("s4", "일정 참여 완료 미승인"),
    PARTICIPATION_COMPLETE_APPROVAL("s5", "일정 참가 완료 승인"),

    ;

    private String code;
    private String desc;

    ParticipantState(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @Override
    public String getId() {
        return this.code;
    }

    @Override
    public String getDesc() {
        return this.desc;
    }

    public static List<ParticipantState> getParticipationCompletionState() {
        return List.of(ParticipantState.PARTICIPATION_COMPLETE_APPROVAL,
                ParticipantState.PARTICIPATION_COMPLETE_UNAPPROVED);
    }
}
