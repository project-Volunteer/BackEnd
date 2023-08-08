package project.volunteer.global.common.dto;

import project.volunteer.global.common.converter.CodeCommonType;

public enum StateResponse implements CodeCommonType {

    AVAILABLE("봉사 팀원 모집 신청 가능"),
    PENDING("봉사 팀원 모집 신청 승인 대기"),
    APPROVED("봉사 팀원 모집 신청 승인 완료"),
    PARTICIPATING("봉사 일정 참여중"),
    CANCELLING("봉사 일정 취소 요청"),
    DONE("봉사 팀원 or 봉사 일정 모집 마감"),
    FULL("봉사 팀원 or 봉사 일정 인원 초과"),
    COMPLETE_UNAPPROVED("봉사 일정 참여 완료 미승인"),
    COMPLETE_APPROVED("봉사 일정 참여 완료 승인")

    ,;

    private String des;
    StateResponse(String dec) {
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
}
