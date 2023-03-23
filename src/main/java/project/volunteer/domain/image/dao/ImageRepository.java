package project.volunteer.domain.image.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import project.volunteer.domain.image.domain.Image;

public interface ImageRepository extends JpaRepository<Image, Long> {
}
