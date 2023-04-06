package project.volunteer.global.common.converter;

import project.volunteer.global.common.component.State;

import javax.persistence.Converter;

@Converter
public class StateConverter extends AbstractLegacyEnumAttributeConverter<State> {

    private static final String ENUM_NAME = "참가 상태";

    public StateConverter(){
        super(State.class, ENUM_NAME);
    }
}
