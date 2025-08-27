package bigmac.urlmodifierbackend.domain.user.service;

import bigmac.urlmodifierbackend.domain.user.dto.response.MyPageResponse;
import bigmac.urlmodifierbackend.domain.user.model.User;

public interface MyPageService {

    MyPageResponse getMyPage(User user);
}
