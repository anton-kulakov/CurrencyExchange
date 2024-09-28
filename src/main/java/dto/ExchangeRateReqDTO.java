package dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExchangeRateReqDTO {
    private static final BigDecimal MIN_POSITIVE_RATE = new BigDecimal("0.000001");
    String baseCurrencyCode;
    String targetCurrencyCode;
    BigDecimal rate;

    public static BigDecimal getMinPositiveRate() {
        return MIN_POSITIVE_RATE;
    }
}
