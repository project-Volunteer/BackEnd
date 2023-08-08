package project.volunteer.domain.confirmation.dao;

import project.volunteer.global.common.component.RealWorkCode;

public interface ConfirmationRepositoryCustom {

    Boolean existsCheck(Long userNo, RealWorkCode code, Long no);
}
