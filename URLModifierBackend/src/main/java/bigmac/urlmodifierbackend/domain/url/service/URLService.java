package bigmac.urlmodifierbackend.domain.url.service;

import bigmac.urlmodifierbackend.domain.url.dto.request.CustomURLRequest;
import bigmac.urlmodifierbackend.domain.url.model.URL;
import bigmac.urlmodifierbackend.domain.user.model.User;
import java.util.Optional;

public interface URLService {

    URL makeURLShort(User user, String url);

    URL makeCustomURL(User user, CustomURLRequest customURLRequest);

    void deleteURL(User user, String shortenedURL);

    Optional<URL> getOriginURLByShortURL(String shortUrl);
}