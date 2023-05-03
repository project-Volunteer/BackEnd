package project.volunteer.global.infra.s3;

import lombok.Getter;
import project.volunteer.global.error.exception.BaseException;
import project.volunteer.global.error.exception.ErrorCode;

import java.util.Arrays;

@Getter
public enum FileFolder {

    USER_IMAGES("1"), RECRUITMENT_IMAGES("2"), LOG_IMAGES("3");

    private final String code;
    FileFolder(String code) {
        this.code = code;
    }

    public static FileFolder of(String code){
        return Arrays.stream(FileFolder.values())
                .filter(v -> v.getCode().equals(code))
                .findAny()
                .orElseThrow(() -> new BaseException(ErrorCode.NOT_FOUND_FILE_FOLDER_OfCode, String.format("Search File Code = [%s]", code)));
    }

}
