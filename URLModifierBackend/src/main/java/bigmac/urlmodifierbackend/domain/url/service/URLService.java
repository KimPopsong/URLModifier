package bigmac.urlmodifierbackend.domain.url.service;

import bigmac.urlmodifierbackend.domain.url.model.URL;
import bigmac.urlmodifierbackend.domain.user.model.User;
import java.util.Optional;

public interface URLService {

    URL makeURLShort(String url, User user);

    Optional<URL> getOriginURLByShortURL(String shortUrl);
}