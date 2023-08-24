package project.volunteer.domain.confirmation.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.confirmation.dao.ConfirmationRepository;
import project.volunteer.domain.confirmation.domain.Confirmation;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.common.component.RealWorkCode;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ConfirmationServiceImpl implements ConfirmationService{
    private final ConfirmationRepository confirmationRepository;

    @Override
    public void addConfirmation(User user, RealWorkCode code, Long no) {
        //유무 검증
        if(confirmationRepository.existsCheck(user.getUserNo(), RealWorkCode.NOTICE, no)){
            throw new BusinessException(ErrorCode.INVALID_CONFIRMATION,
                    String.format("code = [%s], No = [%d], userNo = [%d]", code.getId(), no, user.getUserNo()));
        }

        Confirmation createConfirmation = Confirmation.createConfirmation(code, no);
        createConfirmation.setUser(user);
        confirmationRepository.save(createConfirmation);
    }

    @Override
    public void deleteConfirmation(Long userNo, RealWorkCode code, Long no) {
        Confirmation confirmation = confirmationRepository.findConfirmation(userNo, code, no)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXIST_CONFIRMATION,
                        String.format("code = [%s], no = [%d], userNo = [%d]", code.getId(), no, userNo)));

        confirmationRepository.delete(confirmation);
    }

    @Override
    public void deleteAllConfirmation(RealWorkCode code, List<Long> nos) {
        confirmationRepository.deleteAllByRealWorkCodeAndNoIn(code, nos);
    }

}
