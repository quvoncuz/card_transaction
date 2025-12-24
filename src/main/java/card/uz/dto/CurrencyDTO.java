package card.uz.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@ToString
public class CurrencyDTO {

    private Integer id;

    private String Code;
    private String Ccy;

    @JsonProperty("CcyNm_UZ")
    private String nameUz;

    private String Nominal;
    private String Rate;
    private String Diff;
    private String Date;
}
