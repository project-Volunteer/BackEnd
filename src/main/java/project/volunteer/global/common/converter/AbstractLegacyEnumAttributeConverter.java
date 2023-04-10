package project.volunteer.global.common.converter;

import lombok.Getter;
import project.volunteer.global.util.LegacyCodeEnumValueConverterUtils;

import javax.persistence.AttributeConverter;

/**
 * 공통 컨버터
 */
@Getter
public class AbstractLegacyEnumAttributeConverter<E extends Enum<E> & LegacyCodeCommonType> implements AttributeConverter<E, String> {

    /**
     * 대상 Enum 클래스 객체{@link Class}
     */
    private Class<E> targetEnumClass;

    /**
     * {@NotNull Enum} Enum에 대한 오류 메시지 출력에 사용
     */
    private String enumName;

    public AbstractLegacyEnumAttributeConverter(Class<E> element, String enumName) {
        this.targetEnumClass = element;
        this.enumName = enumName;
    }

    @Override
    public String convertToDatabaseColumn(E attribute) {
        //모든 Enum 타입 컬럼엔 null, "" 들어갈수 없음.
        if(attribute==null){
            throw new IllegalArgumentException(String.format("[%s]는 NULL로 저장될 수 없습니다.",enumName));
        }
        return LegacyCodeEnumValueConverterUtils.toLegacyCode(attribute);
    }

    @Override
    public E convertToEntityAttribute(String dbData) {
        return LegacyCodeEnumValueConverterUtils.ofLegacyCode(targetEnumClass, dbData);
    }
}
