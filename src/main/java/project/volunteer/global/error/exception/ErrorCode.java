package project.volunteer.global.error.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    //common
    UNKNOWN_ENUM_VALUE(HttpStatus.BAD_REQUEST, "mismatch.enumValue"),
    UNKNOWN_ENUM_CODE(HttpStatus.BAD_REQUEST, "mismatch.enumCode"),
    INVALID_ATTRIBUTE(HttpStatus.BAD_REQUEST, "invalid.data"), //modelAttribute
    INVALID_PAYLOAD(HttpStatus.BAD_REQUEST, "invalid.data"), //requestBody
    NOT_NULL_COLUMN(HttpStatus.INTERNAL_SERVER_ERROR, "notNull.column"),

    //모집글 관련
    NOT_EXIST_RECRUITMENT(HttpStatus.BAD_REQUEST, "notExist.recruitment"),
    FORBIDDEN_RECRUITMENT(HttpStatus.FORBIDDEN, "forbidden.recruitment"),
    FORBIDDEN_RECRUITMENT_TEAM(HttpStatus.FORBIDDEN, "forbidden.recruitment.team"),

    //사용자 관련
    NOT_EXIST_USER(HttpStatus.BAD_REQUEST, "notExist.user"),
    UNAUTHORIZED_USER(HttpStatus.UNAUTHORIZED, "unauthorized.user"),

    //파일 관련
    INVALID_FILE_FORMAT(HttpStatus.BAD_REQUEST, "invalid.file.format"),
    NOT_FOUND_FILE_FOLDER(HttpStatus.INTERNAL_SERVER_ERROR, "notFound.file.folder"),
    NOT_FOUND_FILE_FOLDER_OfCode(HttpStatus.INTERNAL_SERVER_ERROR, "notFound.file.folder"),
    S3_UPLOAD_IO_EX(HttpStatus.INTERNAL_SERVER_ERROR, "io.file.upload"),

    //모집글 팀원 관련
    DUPLICATE_PARTICIPATION(HttpStatus.BAD_REQUEST, "duplicate.participation"),
    INVALID_STATE(HttpStatus.BAD_REQUEST, "invalid.state"),
    INSUFFICIENT_CAPACITY(HttpStatus.BAD_REQUEST, "insufficient.capacity"),
    INSUFFICIENT_APPROVAL_CAPACITY(HttpStatus.BAD_REQUEST, "insufficient.approval.capacity"),

    ;

    private final HttpStatus httpStatus; //상태 코드
    private final String propertiesCode; //메시지 코드

}
