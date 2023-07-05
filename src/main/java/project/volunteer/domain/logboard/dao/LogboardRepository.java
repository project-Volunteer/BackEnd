package project.volunteer.domain.logboard.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import project.volunteer.domain.logboard.domain.Logboard;

public interface LogboardRepository extends JpaRepository<Logboard, Long>, CustomLogboardRepository {
	
}
