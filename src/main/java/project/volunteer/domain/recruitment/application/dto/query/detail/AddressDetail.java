package project.volunteer.domain.recruitment.application.dto.query.detail;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.volunteer.global.common.component.Address;
import project.volunteer.global.common.component.Coordinate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AddressDetail {
    private String sido;
    private String sigungu;
    private String details;
    private String fullName;
    private Float latitude;
    private Float longitude;

    public AddressDetail(Address address, Coordinate coordinate){
        this.sido = address.getSido();
        this.sigungu = address.getSigungu();
        this.details = address.getDetails();
        this.fullName = address.getFullName();
        this.latitude = coordinate.getLatitude();
        this.longitude = coordinate.getLongitude();
    }
}
