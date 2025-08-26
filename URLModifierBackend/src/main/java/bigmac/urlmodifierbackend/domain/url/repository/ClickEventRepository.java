package bigmac.urlmodifierbackend.domain.url.repository;

import bigmac.urlmodifierbackend.domain.url.model.ClickEvent;
import bigmac.urlmodifierbackend.domain.url.model.URL;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
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

}