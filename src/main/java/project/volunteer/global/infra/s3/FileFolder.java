package project.volunteer.global.infra.s3;

import lombok.Getter;

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
                .orElseThrow(() -> new FolderNotFoundException(String.format("Not found match fileFolder=[%s]",code)));
    }

}
