package utils;

import java.util.Currency;
import java.util.Set;
import java.util.stream.Collectors;

public class CurrencyCodesValidator {
    private static Set<String> currencyCodes;
    public static boolean isCurrencyCodeValid(String code) {
        if (currencyCodes == null) {
            Set<Currency> currencies = Currency.getAvailableCurrencies();
            currencyCodes = currencies.stream()
                    .map(Currency::getCurrencyCode)
                    .collect(Collectors.toSet());
        }

        return currencyCodes.contains(code);
    }
}
