package project.volunteer.domain.sehedule.application.dto.query;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import project.volunteer.global.common.component.Address;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AddressDetail {
    private String sido;
    private String sigungu;
    private String details;
    private String fullName;

    public static AddressDetail from(Address address) {
        return new AddressDetail(address.getSido(), address.getSigungu(), address.getDetails(), address.getFullName());
    }

}
