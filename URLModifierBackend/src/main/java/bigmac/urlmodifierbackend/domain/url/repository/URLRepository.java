package bigmac.urlmodifierbackend.domain.url.repository;

import bigmac.urlmodifierbackend.domain.url.model.URL;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface URLRepository extends JpaRepository<URL, Long> {
    Optional<URL> findByOriginURL(String url);

    Optional<URL> findByShortenedURL(String shortUrl);
}
