package project.volunteer.domain.user.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import project.volunteer.domain.user.domain.User;

public interface UserRepository extends JpaRepository<User,Long> {
}
