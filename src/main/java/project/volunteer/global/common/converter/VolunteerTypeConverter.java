package project.volunteer.global.common.converter;

import project.volunteer.domain.recruitment.domain.VolunteerType;
import javax.persistence.Converter;

@Converter
public class VolunteerTypeConverter extends AbstractLegacyEnumAttributeConverter<VolunteerType> {

    public static final String ENUM_NAME = "봉사자 유형";

    public VolunteerTypeConverter(){
        super(VolunteerType.class, ENUM_NAME);
    }
}
