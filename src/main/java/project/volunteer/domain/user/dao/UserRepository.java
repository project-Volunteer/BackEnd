package project.volunteer.domain.user.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import project.volunteer.domain.user.domain.User;


public interface UserRepository extends JpaRepository<User,Long> {

    Optional<User> findByEmail(String email);
    
	Optional<User> findByEmailAndProviderId(String email, String providerId);

	Optional<User> findById(String id);

	Optional<User> findByRefreshToken(String refreshToken);

	Optional<User> findByUserNo(Long userNo);
	
}
