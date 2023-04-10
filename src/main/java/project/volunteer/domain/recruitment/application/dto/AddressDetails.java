package project.volunteer.domain.recruitment.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import project.volunteer.global.common.component.Address;
import project.volunteer.global.common.component.Coordinate;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class AddressDetails {

    private String sido;
    private String sigungu;
    private String details;
    private Float latitude;
    private Float longitude;

    public AddressDetails(Address address, Coordinate coordinate){
        this.sido = address.getSido();
        this.sigungu = address.getSigungu();
        this.details = address.getDetails();
        this.latitude = coordinate.getLatitude();
        this.longitude = coordinate.getLongitude();
    }
}
