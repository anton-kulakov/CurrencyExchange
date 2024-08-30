package dto;

import entity.Currency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExchangeRateRespDTO {
    int id;
    Currency baseCurrency;
    Currency targetCurrency;
    BigDecimal rate;
}
