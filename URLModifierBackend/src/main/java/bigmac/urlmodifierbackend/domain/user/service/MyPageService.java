package bigmac.urlmodifierbackend.domain.user.service;

import bigmac.urlmodifierbackend.domain.url.dto.response.URLDetailResponse;
import bigmac.urlmodifierbackend.domain.user.dto.response.MyPageResponse;
import bigmac.urlmodifierbackend.domain.user.model.User;

public interface MyPageService {

    MyPageResponse getMyPage(User user);

    void deleteUrl(User user, Long urlId);

    URLDetailResponse detailUrl(User user, Long urlId);
}
