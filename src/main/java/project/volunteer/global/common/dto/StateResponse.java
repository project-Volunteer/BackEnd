package project.volunteer.global.common.dto;

import project.volunteer.global.common.converter.CodeCommonType;

public enum StateResponse implements CodeCommonType {

    AVAILABLE("신청 가능"),
    PENDING("승인 대기"),
    APPROVED("승인 완료"),
    PARTICIPATING("참여중"),
    CANCELLING("취소 요청 중"),
    DONE("모집 마감"),
    FULL("인원 초과"),
    COMPLETE_UNAPPROVED("참여 완료 미승인"),
    COMPLETE_APPROVED("참여 완료 승인")

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
