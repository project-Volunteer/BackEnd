package project.volunteer.domain.recruitment.converter;

import project.volunteer.domain.recruitment.domain.VolunteeringCategory;
import project.volunteer.global.common.converter.AbstractLegacyEnumAttributeConverter;

import javax.persistence.Converter;

@Converter
public class CategoryConverter extends AbstractLegacyEnumAttributeConverter<VolunteeringCategory> {
    public static final String ENUM_NAME = "봉사 카테고리";

    public CategoryConverter() {
        super(VolunteeringCategory.class, ENUM_NAME);
    }
}
