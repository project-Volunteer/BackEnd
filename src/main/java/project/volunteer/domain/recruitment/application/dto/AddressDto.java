package project.volunteer.domain.recruitment.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import project.volunteer.global.common.component.Address;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class AddressDto {

    private String sido;
    private String sigungu;
    private String details;
    private Float latitude;
    private Float longitude;

    public AddressDto(Address address){
        this.sido = address.getSido();
        this.sigungu = address.getSigungu();
        this.details = address.getDetails();
        this.latitude = address.getLatitude();
        this.longitude = address.getLongitude();
    }
}
