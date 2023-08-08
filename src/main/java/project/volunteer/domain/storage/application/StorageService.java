package project.volunteer.domain.storage.application;

import org.springframework.web.multipart.MultipartFile;
import project.volunteer.global.common.component.RealWorkCode;
import project.volunteer.domain.storage.domain.Storage;

public interface StorageService {

    public Storage addStorage(MultipartFile file, RealWorkCode realWorkCode);

}
