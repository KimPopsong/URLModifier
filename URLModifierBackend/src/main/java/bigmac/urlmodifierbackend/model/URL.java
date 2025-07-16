package bigmac.urlmodifierbackend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
    @JoinColumn(name = "user_id")
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
}