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

    @Column(name = "origin_url", nullable = false)
    private String originURL;

    @Column(name = "shortened_url", nullable = false)
    private String shortenedURL;

    @Column(name = "qr_code", nullable = false)
    private String qrCode;
}
