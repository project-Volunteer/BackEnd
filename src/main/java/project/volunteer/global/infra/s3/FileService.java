package project.volunteer.global.infra.s3;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    //파일 업로드
    String uploadFile(MultipartFile file, FileFolder fileFolder);

    //파일 삭제
    void deleteFile(String fileName);

    //파일 path 조회
    String getFileUrl(String filaName);

    //폴더 이름 조회
    String getFileFolder(FileFolder fileFolder);

}
