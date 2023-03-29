package project.volunteer.global.infra.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AwsS3Service implements FileService{

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.s3.folder.folderName1}")
    private String userFolder;

    @Value("${cloud.aws.s3.folder.folderName2}")
    private String recruitmentFolder;

    @Value(("${cloud.aws.s3.folder.folderName3}"))
    private String logFolder;

    private final AmazonS3 amazonS3;

    @Override
    public String uploadFile(MultipartFile file, FileFolder fileFolder) {

        //1. 저장 파일 이름 생성
        String storageFileName = getFileFolder(fileFolder) + createStoreFileName(file.getOriginalFilename());

        //2. 파일의 추가정보(메타데이터 생성)
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(file.getSize());
        objectMetadata.setContentType(file.getContentType());

        //3. 파일 업로드
        try(InputStream inputStream = file.getInputStream()){
            amazonS3.putObject(
                    new PutObjectRequest(bucket, storageFileName, inputStream, objectMetadata)
                            .withCannedAcl(CannedAccessControlList.PublicRead)
            );
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format("파일 변환 중 에러가 발생했습니다. (%s)", file.getOriginalFilename()));
        }
        return storageFileName;
    }

    @Override
    public void deleteFile(String fileName) {
        amazonS3.deleteObject(new DeleteObjectRequest(bucket, fileName));
    }

    @Override
    public String getFileUrl(String filaName) {
       return amazonS3.getUrl(bucket, filaName).toString();
    }

    @Override
    public String getFileFolder(FileFolder fileFolder) {

        String folder = "";
        if(fileFolder == FileFolder.USER_IMAGES){
            folder = userFolder;
        }else if(fileFolder == FileFolder.RECRUITMENT_IMAGES){
            folder = recruitmentFolder;
        }else if(fileFolder == FileFolder.LOG_IMAGES){
            folder = logFolder;
        }else {
            throw new FolderNotFoundException("일치하는 폴더가 없습니다.");
        }
        return folder;
    }

    private String createStoreFileName(String originalFileName) {
        String storageDate = LocalDate.now().format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
        return UUID.randomUUID().toString() + "_" + storageDate + getOriginalFileExtension(originalFileName);
    }

    private String getOriginalFileExtension(String originalFileName) {
        try {
            return originalFileName.substring(originalFileName.lastIndexOf("."));
        }catch(StringIndexOutOfBoundsException e) {
            throw new IllegalArgumentException(String.format("잘못된 형식의 파일 (%s) 입니다.", originalFileName));
        }
    }
}
