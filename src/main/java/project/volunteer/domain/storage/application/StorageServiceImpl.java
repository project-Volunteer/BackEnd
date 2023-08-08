package project.volunteer.domain.storage.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import project.volunteer.global.common.component.RealWorkCode;
import project.volunteer.domain.storage.dao.StorageRepository;
import project.volunteer.domain.storage.domain.Storage;
import project.volunteer.global.infra.s3.FileFolder;
import project.volunteer.global.infra.s3.FileService;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StorageServiceImpl implements StorageService {

    private final FileService fileService;
    private final StorageRepository storageRepository;

    @Override
    @Transactional
    public Storage addStorage(MultipartFile file, RealWorkCode realWorkCode) {

        //이미지 저장소 저장
        String storageFileName = fileService.uploadFile(file, FileFolder.of(realWorkCode.getCode()));
        //이미지 path
        String filePath = fileService.getFileUrl(storageFileName);

        return storageRepository.save(
                Storage.builder()
                        .imagePath(filePath)
                        .realImageName(file.getOriginalFilename())
                        .fakeImageName(storageFileName)
                        .extName(getSaveFileExtension(storageFileName))
                        .build());
    }

    //FileService 에서 검사를 마쳤기 때문에 에러 발생할 경우 없음.
    private String getSaveFileExtension(String storageFileName){
        return storageFileName.substring(storageFileName.lastIndexOf("."));
    }

}
