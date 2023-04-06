package project.volunteer.global.common.converter;

public interface LegacyCodeCommonType {

    /**
     * 공통으로 존재하는 Getter 기능
     * Enum 클래스는 상속이 불가능(Enum<T>를 이미 상속)
     * Enum 기능을 계승해서 사용하기 위해 인터페이스 사용
     * @Return string(legacy code, view name)
     */
    String getLegacyCode();
    String getDesc();

}
