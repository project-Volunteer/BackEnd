package project.volunteer.domain.image.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import project.volunteer.domain.image.domain.Image;
import project.volunteer.domain.image.domain.RealWorkCode;

import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Long> {

    public Optional<Image> findByRealWorkCodeAndNo(RealWorkCode realWorkCode, Long no);
}
