package dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExchangeReqDTO {
    private static final BigDecimal MIN_POSITIVE_AMOUNT = new BigDecimal("0.01");
    String from;
    String to;
    BigDecimal amount;

    public static BigDecimal getMinPositiveAmount() {
        return MIN_POSITIVE_AMOUNT;
    }

}
