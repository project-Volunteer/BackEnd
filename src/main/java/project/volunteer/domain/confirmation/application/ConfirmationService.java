package project.volunteer.domain.confirmation.application;

import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.RealWorkCode;

public interface ConfirmationService {
    //확인
    public void addConfirmation(User user, RealWorkCode code, Long no);

    public void deleteConfirmation(Long userNo, RealWorkCode code, Long no);
}
