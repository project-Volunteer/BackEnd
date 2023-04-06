package project.volunteer.global.common.converter;

import project.volunteer.domain.image.domain.RealWorkCode;
import javax.persistence.Converter;

@Converter
public class RealWorkCodeConverter extends AbstractLegacyEnumAttributeConverter<RealWorkCode> {
    public static final String ENUM_NAME = "이미지 타입";

    public RealWorkCodeConverter() {
        super(RealWorkCode.class, ENUM_NAME);
    }
}
