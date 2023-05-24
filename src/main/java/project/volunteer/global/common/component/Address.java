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

    @Builder
    public Address(String sido, String sigungu, String details){
        this.sido = sido;
        this.sigungu = sigungu;
        this.details = details;
    }

    public static Address createAddress(String sido, String sigungu, String details){
        Address address = new Address();
        address.sido = sido;
        address.sigungu  = sigungu;
        address.details = details;
        return address;
    }
}
