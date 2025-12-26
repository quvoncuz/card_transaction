package card.uz.entity;

import card.uz.enums.Currency;
import card.uz.enums.Purpose;
import card.uz.enums.TransactionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "transaction")
@Getter
@Setter
public class    TransactionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "external_id", nullable = false)
    private String externalId;

    @Column(name = "card_id")
    private String cardId;
    @ManyToOne
    @JoinColumn(name = "card_id", insertable = false, updatable = false)
    private CardEntity card;

    @Column
    private Long afterBalance;

    @Column
    private Long amount;

    @Column
    private Currency currency;

    @Column
    private Purpose purpose;

    @Column(name = "transaction_type")
    private TransactionType transactionType;

    @Column(name = "exchange_rate")
    private Long exchangeRate;

    @Column(name = "created_date")
    @CreationTimestamp
    private LocalDateTime createdDate;
}
