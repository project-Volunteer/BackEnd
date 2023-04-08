package project.volunteer.domain.user.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import project.volunteer.domain.user.domain.User;

public interface UserRepository extends JpaRepository<User,Long> {

	Optional<User> findByEmailAndProviderId(String email, String providerId);

	Optional<User> findByProviderId(String providerId);

	Optional<User> findByUserNo(Long userNo);
}
