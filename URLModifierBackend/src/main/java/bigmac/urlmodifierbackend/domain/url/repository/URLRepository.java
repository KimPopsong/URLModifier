package bigmac.urlmodifierbackend.domain.url.repository;

import bigmac.urlmodifierbackend.domain.url.model.URL;
import bigmac.urlmodifierbackend.domain.user.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface URLRepository extends JpaRepository<URL, Long> {

    Optional<URL> findByOriginURL(String url);

    Optional<URL> findByShortenedURL(String shortUrl);

    List<URL> findByUser(User user);

    Optional<URL> findByUserAndOriginURL(User user, String originURL);

    Optional<URL> findFirstByOriginURLAndUserIsNull(String originURL);
}
