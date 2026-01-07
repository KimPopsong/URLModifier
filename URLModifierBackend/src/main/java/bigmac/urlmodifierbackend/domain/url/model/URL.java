package bigmac.urlmodifierbackend.domain.url.model;

import bigmac.urlmodifierbackend.domain.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "url", indexes = {
    @Index(name = "idx_url_origin_url", columnList = "origin_url"),
    @Index(name = "idx_url_user_origin", columnList = "users,origin_url"),
    @Index(name = "idx_url_user", columnList = "users"),
    @Index(name = "idx_url_origin_user_null", columnList = "origin_url", unique = false)
})
public class URL {

    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users", nullable = true)
    private User user;

    @Column(name = "origin_url", nullable = false, columnDefinition = "TEXT")
    private String originURL;

    @Column(name = "shortened_url", nullable = false, unique = true, length = 30)
    private String shortenedURL;

    @Column(name = "qr_code", nullable = false, columnDefinition = "TEXT")
    private String qrCode;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public URL(long id, User user, String originURL, String shortenedURL, String qrCodeBase64) {
        this.id = id;
        this.user = user;
        this.originURL = originURL;
        this.shortenedURL = shortenedURL;
        this.qrCode = qrCodeBase64;
    }
}