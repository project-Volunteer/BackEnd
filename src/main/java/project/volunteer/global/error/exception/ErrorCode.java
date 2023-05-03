package project.volunteer.global.error.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.Comparator;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    //모집글 관련
    NOT_EXIST_RECRUITMENT(HttpStatus.BAD_REQUEST, "recruitment.not_exist"),

    //사용자 관련
    NOT_EXIST_USER(HttpStatus.BAD_REQUEST, "user.not_exist"),
    UNAUTHORIZED_USER(HttpStatus.UNAUTHORIZED, "user.unauthorized"),

    //파일 관련
    INVALID_FILE_FORMAT(HttpStatus.BAD_REQUEST, "file.format.invalid"),
    NOT_FOUND_FILE_FOLDER(HttpStatus.INTERNAL_SERVER_ERROR, "file.folder.not_found"),
    S3_UPLOAD_IO_EX(HttpStatus.INTERNAL_SERVER_ERROR, "file.upload.io_ex")

    ;

    private final HttpStatus httpStatus; //상태 코드
    private final String propertiesCode; //메시지 코드

}
