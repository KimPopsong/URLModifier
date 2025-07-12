package bigmac.urlmodifierbackend.repository;

import bigmac.urlmodifierbackend.model.URL;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface URLRepository extends JpaRepository<URL, Long> {
    Optional<URL> findByOriginURL(String url);

    Optional<URL> findByShortenedURL(String shortUrl);
}
