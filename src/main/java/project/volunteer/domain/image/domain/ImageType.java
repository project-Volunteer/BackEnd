package project.volunteer.domain.image.domain;

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
                .orElseThrow(() -> new IllegalArgumentException(String.format("Not found match image type=[%s]",value)));
    }
}
