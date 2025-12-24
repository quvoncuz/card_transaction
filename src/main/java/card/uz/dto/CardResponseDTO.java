package card.uz.dto;

import card.uz.enums.Currency;
import card.uz.enums.Status;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CardResponseDTO {
    private Long userId;
    private Status status;
    private Long balance;
    private Currency currency;
}
