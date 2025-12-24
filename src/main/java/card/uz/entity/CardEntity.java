package card.uz.entity;

import card.uz.enums.Currency;
import card.uz.enums.Status;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "card")
@Getter
@Setter
public class CardEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "user_id")
    private Long userId;
    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private UserEntity user;

    @Column
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "balance")
    private Long balance;

    @Column(name = "currency")
    @Enumerated(EnumType.STRING)
    private Currency currency;
}
