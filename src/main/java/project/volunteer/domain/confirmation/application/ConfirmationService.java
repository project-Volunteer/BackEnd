package project.volunteer.domain.confirmation.application;

import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.RealWorkCode;

import java.util.List;

public interface ConfirmationService {
    //확인
    public void addConfirmation(User user, RealWorkCode code, Long no);

    public void deleteConfirmation(Long userNo, RealWorkCode code, Long no);

    public void deleteAllConfirmation(RealWorkCode code, List<Long> nos);
}
