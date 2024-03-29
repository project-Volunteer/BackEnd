package project.volunteer.global.common.component;

import lombok.Getter;
import project.volunteer.global.common.converter.CodeCommonType;

@Getter
public enum RealWorkCode implements CodeCommonType {
    USER("1", "유저"),
    RECRUITMENT("2", "모집글"),
    LOG("3", "로그"),
    NOTICE("4", "공지사항")

    ,;

    private String code;
    private String des;

    RealWorkCode(String code, String des) {
        this.code = code;
        this.des = des;
    }

    @Override
    public String getId() {
        return this.code;
    }

    @Override
    public String getDesc() {
        return this.des;
    }
}
