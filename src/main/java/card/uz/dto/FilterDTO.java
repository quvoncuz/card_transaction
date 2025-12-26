package card.uz.dto;

import card.uz.enums.Currency;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FilterDTO {
    private String transactionId;
    private String externalId;
    private Currency currency;
}
