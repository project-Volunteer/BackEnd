package project.volunteer.global.common.validate;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import project.volunteer.domain.logboard.dao.LogboardRepository;
import project.volunteer.domain.logboard.domain.Logboard;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;

@Component
@RequiredArgsConstructor
public class LogboardValidate {
    private final LogboardRepository logboardRepository;

    // 로그 이미 등록 했는지 여부 검증
    public void validateLogboardAlreadyWrite(Long userNo, Long scheduleNo) {
        if(logboardRepository.existsLogboardByUserNoAndSchedulNo(userNo, scheduleNo)) {
            throw new BusinessException(ErrorCode.DUPLICATE_LOGBOARD,
                    String.format("UserNo = [%d], ScheduleNo = [%d]", userNo, scheduleNo));
        }
    }

    // 봉사 로그 작성자 여부 체크
    public void validateEqualParamUserNoAndFindUserNo(Long ParamUserNo, Logboard findLogboard) {
        if(!ParamUserNo.equals(findLogboard.getWriter().getUserNo())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_LOGBOARD,
                    String.format("forbidden logboard userno=[%d], logboardno=[%d]", ParamUserNo, findLogboard.getLogboardNo()));
        }
    }

    // 봉사 로그 존재 유무 확인
    public Logboard validateAndGetLogboard(Long logboardNo) {
        return logboardRepository.findById(logboardNo)
                .orElseThrow(()-> new BusinessException(ErrorCode.NOT_EXIST_LOGBOARD,
                        String.format("not found logboard = [%d]", logboardNo)));
    }


}
