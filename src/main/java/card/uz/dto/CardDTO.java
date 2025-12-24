package card.uz.dto;

import card.uz.enums.Currency;
import card.uz.enums.Status;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@ToString
public class CardDTO {
    @NotBlank(message = "User ID cannot be blank")
    private Long userId;
    private String cardId;
    private Status status;
    private Long balance;
    private Currency currency;
}
