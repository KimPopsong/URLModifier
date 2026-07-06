package bigmac.urlmodifierbackend.domain.url.repository;

import bigmac.urlmodifierbackend.domain.url.model.ClickEvent;
import bigmac.urlmodifierbackend.domain.url.model.URL;
import bigmac.urlmodifierbackend.domain.user.model.User;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClickEventRepository extends JpaRepository<ClickEvent, Long> {

    // 특정 URL의 시간대별 클릭 수 (시간 단위)
    @Query("SELECT FUNCTION('DATE_TRUNC', 'hour', c.clickedAt), COUNT(c) " + "FROM ClickEvent c "
        + "WHERE c.url = :url AND c.clickedAt BETWEEN :start AND :end "
        + "GROUP BY FUNCTION('DATE_TRUNC', 'hour', c.clickedAt) "
        + "ORDER BY FUNCTION('DATE_TRUNC', 'hour', c.clickedAt)")
    List<Object[]> countHourlyClicks(@Param("url") URL url, @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end);

    // 특정 URL의 일자별 클릭 수 (DB에서 집계)
    @Query("SELECT FUNCTION('TO_CHAR', c.clickedAt, 'YYYY-MM-DD'), COUNT(c) "
        + "FROM ClickEvent c WHERE c.url = :url "
        + "GROUP BY FUNCTION('TO_CHAR', c.clickedAt, 'YYYY-MM-DD') "
        + "ORDER BY FUNCTION('TO_CHAR', c.clickedAt, 'YYYY-MM-DD')")
    List<Object[]> countDailyClicks(@Param("url") URL url);

    // 사용자의 모든 URL별 클릭 수 (마이페이지용, N+1 방지)
    @Query("SELECT c.url.id, COUNT(c) FROM ClickEvent c WHERE c.url.user = :user "
        + "GROUP BY c.url.id")
    List<Object[]> countByUserGroupByUrl(@Param("user") User user);

    long countByUrl(URL url);

    @Modifying
    @Query("DELETE FROM ClickEvent c WHERE c.url = :url")
    void deleteAllByUrl(@Param("url") URL url);

    @Modifying
    @Query("DELETE FROM ClickEvent c WHERE c.url IN :urls")
    void deleteAllByUrlIn(@Param("urls") List<URL> urls);
}
