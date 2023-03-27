package project.volunteer.domain.image.domain;

public enum ImageType {
    STATIC(0), UPLOAD(1);

    private final Integer value;

    ImageType(int value) {
        this.value = value;
    }

    public static ImageType of(int value){
        for(ImageType type : ImageType.values()){
            if(type.value==value)
                return type;
        }
        throw new IllegalArgumentException("Not found mismatch Image type. value:"+value);
    }
}
