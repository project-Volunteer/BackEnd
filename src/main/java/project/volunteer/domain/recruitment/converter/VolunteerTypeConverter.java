package project.volunteer.domain.recruitment.converter;

import project.volunteer.domain.recruitment.domain.VolunteerType;
import project.volunteer.global.common.converter.AbstractEnumAttributeConverter;

import javax.persistence.Converter;

@Converter
public class VolunteerTypeConverter extends AbstractEnumAttributeConverter<VolunteerType> {

    public static final String ENUM_NAME = "봉사자 유형";

    public VolunteerTypeConverter(){
        super(VolunteerType.class, ENUM_NAME);
    }
}
