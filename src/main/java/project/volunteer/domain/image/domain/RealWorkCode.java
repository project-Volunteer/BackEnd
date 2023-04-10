package project.volunteer.domain.image.domain;

import lombok.Getter;
import project.volunteer.global.common.converter.LegacyCodeCommonType;

@Getter
public enum RealWorkCode implements LegacyCodeCommonType {
    USER("1", "유저"), RECRUITMENT("2", "모집글"), LOG("3", "로그");

    private String code;
    private String des;

    RealWorkCode(String code, String des) {
        this.code = code;
        this.des = des;
    }

    @Override
    public String getLegacyCode() {
        return this.code;
    }

    @Override
    public String getDesc() {
        return this.des;
    }
}
