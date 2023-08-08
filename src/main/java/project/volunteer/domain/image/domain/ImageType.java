package project.volunteer.domain.image.domain;

import project.volunteer.global.common.converter.CodeCommonType;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

import java.util.Arrays;

public enum ImageType implements CodeCommonType {
    STATIC("정적 이미지"), UPLOAD("업로드 이미지");

    private final String des;

    ImageType(String des) {
        this.des = des;
    }

    public static ImageType of(String value){
        return Arrays.stream(ImageType.values())
                .filter(v -> v.getId().equals(value.toUpperCase()))
                .findAny()
                .orElseThrow(() -> new BusinessException(ErrorCode.UNKNOWN_ENUM_VALUE, String.format("ImageType Value = [%s]", value)));
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
