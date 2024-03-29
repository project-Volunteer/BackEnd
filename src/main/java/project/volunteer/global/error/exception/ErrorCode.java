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
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "unsupported.mediaType"),
    MISSING_REQUEST_PARAMETER(HttpStatus.BAD_REQUEST, "missing.request.parameter"),

    //봉사 모집글 관련
    INVALID_TITLE_SIZE(HttpStatus.BAD_REQUEST, "invalid.title.size"),
    INVALID_PERIOD_PARAMETER(HttpStatus.BAD_REQUEST, "invalid.period.parameter"),
    NOT_EXIST_RECRUITMENT(HttpStatus.BAD_REQUEST, "notExist.recruitment"),
    INVALID_PARTICIPATION_NUM(HttpStatus.BAD_REQUEST, "invalid.participation.num"),
    INVALID_CURRENT_PARTICIPATION_NUM(HttpStatus.BAD_REQUEST, "invalid.current.participation.num"),


    EXPIRED_PERIOD_RECRUITMENT(HttpStatus.BAD_REQUEST, "expired.period.recruitment"),
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
    NOT_EXIST_RECRUITMENT_PARTICIPANT(HttpStatus.BAD_REQUEST, "notExist.recruitment.participant"),
    DUPLICATE_RECRUITMENT_PARTICIPATION(HttpStatus.BAD_REQUEST, "duplicate.recruitment.participation"),
    INVALID_STATE(HttpStatus.BAD_REQUEST, "invalid.state"),
    INSUFFICIENT_CAPACITY(HttpStatus.BAD_REQUEST, "insufficient.capacity"),

    //일정 관련
    INVALID_ORGANIZATION_NAME_SIZE(HttpStatus.BAD_REQUEST, "invalid.organizationName.size"),
    INVALID_CONTENT_SIZE(HttpStatus.BAD_REQUEST, "invalid.content.size"),
    EXCEED_PARTICIPATION_NUM_THAN_RECRUITMENT_PARTICIPATION_NUM(HttpStatus.BAD_REQUEST,
            "exceed.participation.num.than.recruitment.participation.num"),
    LESS_PARTICIPATION_NUM_THAN_CURRENT_PARTICIPANT(HttpStatus.BAD_REQUEST,
            "less.participation.num.then.current.participant"),
    EXPIRED_PERIOD_SCHEDULE(HttpStatus.BAD_REQUEST, "expired.period.schedule"),
    NOT_EXIST_SCHEDULE(HttpStatus.BAD_REQUEST, "notExist.schedule"),

    
    //로그보드 관련
    NOT_EXIST_LOGBOARD(HttpStatus.BAD_REQUEST, "notExist.logboard"),
    DUPLICATE_LOGBOARD(HttpStatus.BAD_REQUEST, "duplicate.logboard"),
    INVALID_STATE_LOGBOARD(HttpStatus.BAD_REQUEST, "invalid.state.logboard"),
    FORBIDDEN_LOGBOARD(HttpStatus.FORBIDDEN, "forbidden.logboard"),

    //공지사항 관련
    NOT_EXIST_NOTICE(HttpStatus.BAD_REQUEST, "notExist.notice"),

    //확인 관련
    NOT_EXIST_CONFIRMATION(HttpStatus.BAD_REQUEST, "notExist.confirmation"),
    INVALID_CONFIRMATION(HttpStatus.BAD_REQUEST, "invalid.confirmation"),
    DUPLICATE_CONFIRMATION(HttpStatus.BAD_REQUEST, "duplicate.confirmation"),
    
    //댓글 관련
    NOT_EXIST_PARENT_REPLY(HttpStatus.BAD_REQUEST, "notExist.parentReply"),
    NOT_EXIST_REPLY(HttpStatus.BAD_REQUEST, "notExist.reply"),
    ALREADY_HAS_PARENT_REPLY(HttpStatus.BAD_REQUEST, "already.hasParentReply"),
    FORBIDDEN_REPLY(HttpStatus.FORBIDDEN, "forbidden.reply")

    ,;
    private final HttpStatus httpStatus; //상태 코드
    private final String propertiesCode; //메시지 코드

}
