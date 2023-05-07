package project.volunteer.global.error.exhadler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import project.volunteer.domain.recruitment.api.RecruitmentController;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;
import project.volunteer.global.error.response.BaseErrorResponse;
import javax.servlet.http.HttpServletResponse;


@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice(basePackageClasses = RecruitmentController.class)
public class RecruitmentControllerAdvice {

    private final MessageSource ms;

    //@ModelAttribute 시 Binding error 및 @Valid error 처리
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BindException.class)
    public BaseErrorResponse BindException(BindException e){
        String message = ms.getMessage(ErrorCode.INVALID_ATTRIBUTE.getPropertiesCode(), null, null);
        log.info("Error Code = {}, Details = {}", ErrorCode.INVALID_ATTRIBUTE, e.getMessage());

        return new BaseErrorResponse(message);
    }

    //사용자 정의 예외 처리
    @ExceptionHandler(BusinessException.class)
    public BaseErrorResponse BaseException(BusinessException e, HttpServletResponse response){
        //상태코드 설정
        response.setStatus(e.getErrorCode().getHttpStatus().value());

        //응답 메시지 제작
        String message = ms.getMessage(e.getErrorCode().getPropertiesCode(), e.getArgs(), null);
        log.info("Error Code = {} , Details = {}", e.getErrorCode().name(), e.getDetails());

        return new BaseErrorResponse(message);
    }
}
