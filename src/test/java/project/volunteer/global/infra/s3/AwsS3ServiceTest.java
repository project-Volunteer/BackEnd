package project.volunteer.global.infra.s3;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import java.io.FileInputStream;
import java.io.IOException;

@SpringBootTest
class AwsS3ServiceTest {

    @Autowired FileService fileService;

    private MockMultipartFile getMockMultipartFile() throws IOException {
        return new MockMultipartFile(
                "file", "file.PNG", "image/jpg", new FileInputStream("src/main/resources/static/test/file.PNG"));
    }
    private MockMultipartFile getFailMockMultipartFile() throws IOException {
        return new MockMultipartFile(
                "file", "filejpg", "image/jpg", new FileInputStream("src/main/resources/static/test/file.PNG"));
    }
    @Test
    public void AWS_S3_이미지_저장_성공() throws IOException {
        //given, when
        String storageFileName = fileService.uploadFile(getMockMultipartFile(), FileFolder.RECRUITMENT_IMAGES);

        //then
        //너무 못작성했는데...움
        String fileUrl = fileService.getFileUrl(storageFileName);
        String testPath = "https://volunteer-project.s3.ap-northeast-2.amazonaws.com/" + storageFileName;
        Assertions.assertThat(testPath).isEqualTo(fileUrl);

        //finally
        fileService.deleteFile(storageFileName);
    }

    @Test
    public void AWS_S3_이미지_저장_실패_파일확장자에러() throws IOException {

        Assertions.assertThatThrownBy(() -> fileService.uploadFile(getFailMockMultipartFile(), FileFolder.RECRUITMENT_IMAGES))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("잘못된 형식의 파일");

    }

}