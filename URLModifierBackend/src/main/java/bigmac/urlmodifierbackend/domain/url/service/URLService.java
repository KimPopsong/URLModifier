package bigmac.urlmodifierbackend.domain.url.service;

import bigmac.urlmodifierbackend.domain.url.dto.request.CustomURLRequest;
import bigmac.urlmodifierbackend.domain.url.dto.response.URLDetailResponse;
import bigmac.urlmodifierbackend.domain.url.model.URL;
import bigmac.urlmodifierbackend.domain.user.model.User;
import java.util.Optional;

public interface URLService {

    URL makeURLShort(User user, String url);

    URL makeCustomURL(User user, CustomURLRequest customURLRequest);

    Optional<URL> getOriginURLByShortURL(String shortenedUrl);

    URL redirectToOriginal(String referrer, String userAgent, String ipAddress,
        String shortenedUrl);

    void deleteUrl(User user, Long urlId);

    URLDetailResponse detailUrl(User user, Long urlId);
}