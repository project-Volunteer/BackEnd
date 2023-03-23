package project.volunteer.domain.storage.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import project.volunteer.domain.storage.domain.Storage;

public interface StorageRepository extends JpaRepository<Storage, Long> {
}
