package bigmac.urlmodifierbackend.service;

import bigmac.urlmodifierbackend.model.URL;
import bigmac.urlmodifierbackend.model.User;

import java.util.Optional;

public interface URLService {
    URL makeURLShort(String url, User user);

    Optional<URL> getOriginURLByShortURL(String shortUrl);
}