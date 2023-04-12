package project.volunteer.domain.user.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import project.volunteer.domain.user.domain.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {

    public Optional<User> findByEmail(String email);
    
	Optional<User> findByEmailAndProviderId(String email, String providerId);

	Optional<User> findByProviderId(String providerId);

	Optional<User> findByUserNo(Long userNo);
}
