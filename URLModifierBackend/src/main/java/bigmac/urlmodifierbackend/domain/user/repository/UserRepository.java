package bigmac.urlmodifierbackend.domain.user.repository;

import bigmac.urlmodifierbackend.domain.user.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
}
