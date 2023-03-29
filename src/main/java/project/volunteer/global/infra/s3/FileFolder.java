package project.volunteer.global.infra.s3;

import project.volunteer.domain.image.domain.ImageType;
import project.volunteer.domain.image.domain.RealWorkCode;

public enum FileFolder {

    USER_IMAGES("USER"), RECRUITMENT_IMAGES("RECRUITMENT"), LOG_IMAGES("LOG");

    private final String label;
    FileFolder(String label) {
        this.label = label;
    }

    public static FileFolder of(String code){
        for(FileFolder folder : FileFolder.values()){
            if(folder.label.equals(code.toUpperCase()))
                return folder;
        }
        throw new IllegalArgumentException("일치하는 폴더가 없습니다.");
    }

}
