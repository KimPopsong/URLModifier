package bigmac.urlmodifierbackend.domain.url.model;

import bigmac.urlmodifierbackend.domain.user.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "url")
public class URL {
    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @Column(name = "origin_url", nullable = false, columnDefinition = "TEXT")
    private String originURL;

    @Column(name = "shortened_url", nullable = false, columnDefinition = "TEXT")
    private String shortenedURL;

    @Column(name = "custom_url", nullable = true, columnDefinition = "TEXT")
    private String customURL;

    @Column(name = "qr_code", nullable = false, columnDefinition = "TEXT")
    private String qrCode;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public URL(long id, User user, String originURL, String shortenedURL, String qrCodeBase64)
    {
        this.id = id;
        this.user = user;
        this.originURL = originURL;
        this.shortenedURL = shortenedURL;
        this.qrCode = qrCodeBase64;
    }
}