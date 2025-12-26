package card.uz.dto;

import card.uz.enums.Currency;
import card.uz.enums.Purpose;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionDTO {

    private String transactionId;

    private String externalId;

    private String cardId;

    private Long afterBalance;

    private Long amount;

    private Currency currency;

    private Purpose purpose;

    private Double exchangeRate;
}
