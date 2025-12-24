package card.uz.dto;

import card.uz.enums.Currency;
import card.uz.enums.Status;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@ToString
public class CardRequestDTO {
    @NotNull(message = "Missung required field(s): userId")
    private Long userId;
    private Status status;
    private Long initialAmount = 0L;
    private Currency currency = Currency.UZS;
}
