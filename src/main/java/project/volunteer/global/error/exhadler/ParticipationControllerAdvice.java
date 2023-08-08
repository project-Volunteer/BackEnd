package project.volunteer.global.error.exhadler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import project.volunteer.domain.participation.api.ParticipationController;
import project.volunteer.global.error.exception.BusinessException;
import project.volunteer.global.error.exception.ErrorCode;
import project.volunteer.global.error.response.BaseErrorResponse;

import javax.servlet.http.HttpServletResponse;

@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice(basePackageClasses = ParticipationController.class)
public class ParticipationControllerAdvice {

    private final MessageSource ms;

    //@RequestBody 시 @Valid error 처리
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BaseErrorResponse MethodArgumentNotValidException(MethodArgumentNotValidException e){

        String message = ms.getMessage(ErrorCode.INVALID_PAYLOAD.getPropertiesCode(), null, null);
        log.info("Error Code = {}, Details = {}", ErrorCode.INVALID_PAYLOAD, e.getMessage(), e);

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
