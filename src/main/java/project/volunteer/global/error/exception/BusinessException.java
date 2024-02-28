package project.volunteer.global.error.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException{

    //예외 코드
    protected ErrorCode errorCode;

    //예외 message 만들때 사용할 매개변수를 담은 배열
    protected Object[] args;

    //추가 상세 메시지
    protected String details;

    public BusinessException(ErrorCode errorCode){
        this.errorCode = errorCode;
    }
    public BusinessException(ErrorCode errorCode, Object[] args, String details) {
        this.errorCode = errorCode;
        this.args = args;
        this.details = details;
    }
    public BusinessException(ErrorCode errorCode, Object[] args) {
        this.errorCode = errorCode;
        this.args = args;
    }
    public BusinessException(ErrorCode errorCode, String details){
        this.errorCode = errorCode;
        this.details = details;
    }

    @Override
    public String getMessage() {
        return errorCode.name();
    }
}
