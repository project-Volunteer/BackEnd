package project.volunteer.domain.participation.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import project.volunteer.domain.participation.domain.Participant;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
}
