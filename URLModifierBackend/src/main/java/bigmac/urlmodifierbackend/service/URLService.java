package bigmac.urlmodifierbackend.service;

import bigmac.urlmodifierbackend.model.URL;

import java.util.Optional;

public interface URLService {
    URL makeURLShort(String url);

    Optional<URL> getOriginURL(String shortUrl);
}