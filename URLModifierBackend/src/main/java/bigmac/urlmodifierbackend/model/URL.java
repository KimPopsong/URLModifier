package bigmac.urlmodifierbackend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class URL {
    @Id
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "origin_url", nullable = false, columnDefinition = "TEXT")
    private String originURL;

    @Column(name = "shortened_url", nullable = false, columnDefinition = "TEXT")
    private String shortenedURL;

    @Column(name = "qr_code", nullable = false, columnDefinition = "TEXT")
    private String qrCode;

    public URL(Long id, String originURL, String shortenedURL, String qrCode)
    {
        this.id = id;
        this.originURL = originURL;
        this.shortenedURL = shortenedURL;
        this.qrCode = qrCode;
    }
}