package dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExchangeRateReqDTO {
    String baseCurrencyCode;
    String targetCurrencyCode;
    BigDecimal rate;
}
