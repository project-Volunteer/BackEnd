package project.volunteer.domain.image.domain;

import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

import java.util.Arrays;

public enum ImageType {
    STATIC(0), UPLOAD(1);

    private final Integer value;

    ImageType(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public static ImageType of(int value){

        return Arrays.stream(ImageType.values())
                .filter(v -> v.value==value)
                .findAny()
                .orElseThrow(() -> new BusinessException(ErrorCode.UNKNOWN_ENUM_VALUE, String.format("ImageType Value = [%s]", value)));
    }
}
