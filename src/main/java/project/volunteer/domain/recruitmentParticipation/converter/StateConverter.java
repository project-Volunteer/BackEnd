package project.volunteer.domain.recruitmentParticipation.converter;

import project.volunteer.global.common.component.ParticipantState;
import project.volunteer.global.common.converter.AbstractEnumAttributeConverter;

import javax.persistence.Converter;

@Converter
public class StateConverter extends AbstractEnumAttributeConverter<ParticipantState> {

    private static final String ENUM_NAME = "참가 상태";

    public StateConverter(){
        super(ParticipantState.class, ENUM_NAME);
    }
}
