package project.volunteer.global.common.validate;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import project.volunteer.domain.user.dao.UserRepository;
import project.volunteer.domain.user.domain.User;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

@Component
@RequiredArgsConstructor
public class UserValidate {
    private final UserRepository userRepository;

    // 유저 존재 유무 확인
    public User validateAndGetUser(Long userNo) {
        return userRepository.findByUserNo(userNo)
                .orElseThrow(()-> new BusinessException(ErrorCode.NOT_EXIST_USER,
                        String.format("not found user = [%d]", userNo)));
    }
}
