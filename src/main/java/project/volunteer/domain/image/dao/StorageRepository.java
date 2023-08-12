package project.volunteer.domain.image.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import project.volunteer.domain.image.domain.Storage;

public interface StorageRepository extends JpaRepository<Storage, Long> {
}
