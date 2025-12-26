package card.uz.dto;

import card.uz.enums.Currency;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@ToString
public class TopUpFundDTO {
    @NotBlank(message = "external_id")
    private String externalId;
    @NotNull(message = "amount")
    private Long amount;
    private Currency currency;
}
