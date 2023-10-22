package project.volunteer.global.common.component;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Getter
@NoArgsConstructor
@Embeddable
public class Address {

    @Column(length = 5, nullable = false)
    private String sido;

    @Column(length = 10, nullable = false)
    private String sigungu;

    @Column(length = 50, nullable = false)
    private String details;

    @Column(nullable = false)
    private String fullName;

    @Builder
    public Address(String sido, String sigungu, String details, String fullName){
        this.sido = sido;
        this.sigungu = sigungu;
        this.details = details;
        this.fullName = fullName;
    }

    public static Address createAddress(String sido, String sigungu, String details, String fullName){
        return Address.builder()
            .sido(sido)
            .sigungu(sigungu)
            .details(details)
            .fullName(fullName)
            .build();
    }
}
