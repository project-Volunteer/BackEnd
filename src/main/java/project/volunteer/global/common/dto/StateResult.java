package project.volunteer.global.common.dto;

import java.util.Optional;
import project.volunteer.global.common.component.ParticipantState;
import project.volunteer.global.common.converter.CodeCommonType;

public enum StateResult implements CodeCommonType {
    AVAILABLE("봉사 팀원 모집 신청 가능"),
    PENDING("봉사 팀원 모집 신청 승인 대기"),
    APPROVED("봉사 팀원 모집 신청 승인 완료"),
    PARTICIPATING("봉사 일정 참여중"),
    CANCELLING("봉사 일정 취소 요청"),
    DONE("봉사 팀원 or 봉사 일정 모집 마감"),
    FULL("봉사 팀원 or 봉사 일정 인원 초과"),
    COMPLETE_UNAPPROVED("봉사 일정 참여 완료 미승인"),
    COMPLETE_APPROVED("봉사 일정 참여 완료 승인"),
    ;
    private String des;

    StateResult(String dec) {
        this.des = dec;
    }

    @Override
    public String getId() {
        return this.name();
    }

    @Override
    public String getDesc() {
        return this.des;
    }

    public static StateResult getRecruitmentState(Optional<ParticipantState> state, boolean isDone,
                                                  boolean isFull) {
        if (state.isPresent() && state.get().equals(ParticipantState.JOIN_REQUEST)) {
            return StateResult.PENDING;
        }

        if (state.isPresent() && state.get().equals(ParticipantState.JOIN_APPROVAL)) {
            return StateResult.APPROVED;
        }

        if (isDone) {
            return StateResult.DONE;
        }

        if (isFull) {
            return StateResult.FULL;
        }

        return StateResult.AVAILABLE;
    }

    public static StateResult getScheduleState(Optional<ParticipantState> state, boolean isDone,
                                               boolean isFull) {
        if (state.isPresent() && state.get().equals(ParticipantState.PARTICIPATION_COMPLETE_UNAPPROVED)) {
            return StateResult.COMPLETE_UNAPPROVED;
        }

        if (state.isPresent() && state.get().equals(ParticipantState.PARTICIPATION_COMPLETE_APPROVAL)) {
            return StateResult.COMPLETE_APPROVED;
        }

        if (state.isPresent() && state.get().equals(ParticipantState.PARTICIPATING)) {
            return StateResult.PARTICIPATING;
        }

        if (state.isPresent() && state.get().equals(ParticipantState.PARTICIPATION_CANCEL)) {
            return StateResult.CANCELLING;
        }

        if (isDone) {
            return StateResult.DONE;
        }

        if (isFull) {
            return StateResult.FULL;
        }

        return StateResult.AVAILABLE;
    }

}
