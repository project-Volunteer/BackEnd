package project.volunteer.domain.image.converter;

import project.volunteer.global.common.component.RealWorkCode;
import project.volunteer.global.common.converter.AbstractEnumAttributeConverter;

import javax.persistence.Converter;

@Converter
public class RealWorkCodeConverter extends AbstractEnumAttributeConverter<RealWorkCode> {
    public static final String ENUM_NAME = "이미지 타입";

    public RealWorkCodeConverter() {
        super(RealWorkCode.class, ENUM_NAME);
    }
}
