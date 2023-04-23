package project.volunteer.global.error.exhadler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import project.volunteer.global.error.response.BaseErrorResponse;
import project.volunteer.global.jwt.api.RefreshTokenController;

@Slf4j
@RestControllerAdvice(basePackageClasses = RefreshTokenController.class)
public class RefreshTokenControllerAdvice {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalAccessException.class)
    public BaseErrorResponse IllegalAccessException(IllegalAccessException e){
        log.error("Illegal access. {}", e.getMessage());
        return new BaseErrorResponse("Illegal access.");
    }

}
