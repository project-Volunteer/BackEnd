package project.volunteer.domain.confirmation.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import project.volunteer.domain.confirmation.domain.Confirmation;

public interface ConfirmationRepository extends JpaRepository<Confirmation, Long>, ConfirmationRepositoryCustom {

}
