package project.volunteer.global.error.exhadler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import project.volunteer.domain.recruitment.api.RecruitmentController;
import project.volunteer.global.error.response.BaseErrorResponse;

@Slf4j
@RestControllerAdvice(basePackageClasses = RecruitmentController.class)
public class RecruitmentControllerAdvice {

    //추후 에러코드 협약필요(임시)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BaseErrorResponse MethodArgumentNotValidException(MethodArgumentNotValidException e){
        log.error("Invalid form Augment. {}", e.getMessage());
        return new BaseErrorResponse("Invalid form Augment.");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(NullPointerException.class)
    public BaseErrorResponse NullPointerException(NullPointerException e) {
        log.error(e.getMessage());
        return new BaseErrorResponse(e.getMessage());
    }

}
