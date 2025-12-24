package card.uz.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "idempotency_key")
@Getter
@Setter
public class IdempotencyKeyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String key;

    @Column
    private String endpoint;

    @Column(name = "request_hash")
    private String requestHash;

    @Column(name = "response_status")
    private Integer responseStatus;

    @Column(columnDefinition = "TEXT")
    private String responseBody;

    @Column(name = "created_date")
    @CreationTimestamp
    private LocalDateTime createdDate;

    @Column(name = "expiration_date")
    private LocalDateTime expirationDate;
}
