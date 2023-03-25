package project.volunteer.domain.image.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.volunteer.domain.image.domain.RealWorkType;

@Service
@Transactional(readOnly = true)
public class ImageServiceImpl implements ImageService{

    @Override
    public void addImage(RealWorkType type, Long no) {

    }
}
