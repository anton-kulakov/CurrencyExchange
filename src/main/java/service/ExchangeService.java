package service;

import dao.CurrencyDAO;
import dao.ExchangeRateDAO;
import model.Exchange;
import model.ExchangeRate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.Optional;

public class ExchangeService {
    CurrencyDAO currencyDAO = CurrencyDAO.getInstance();
    ExchangeRateDAO exchangeRateDAO = ExchangeRateDAO.getInstance();

    public Optional<Exchange> makeExchange(String from, String to, BigDecimal amount) throws SQLException {
        Optional<ExchangeRate> optionalExchangeRate = getOptionalExchangeRate(from, to);
        Exchange exchange = null;

        if (optionalExchangeRate.isPresent()) {
            BigDecimal rate = optionalExchangeRate.get().getRate();

            exchange = new Exchange(
                    currencyDAO.getByCode(from).get(),
                    currencyDAO.getByCode(to).get(),
                    rate,
                    amount,
                    amount.multiply(rate).setScale(2, RoundingMode.HALF_UP)
            );
        }

        return Optional.ofNullable(exchange);
    }

    private Optional<ExchangeRate> getOptionalExchangeRate(String from, String to) throws SQLException {
        Optional<ExchangeRate> optionalExchangeRate = getDirectExchangeRate(from, to);

        if (optionalExchangeRate.isEmpty()) {
            optionalExchangeRate = getReversedExchangeRate(from, to);
        }

        if (optionalExchangeRate.isEmpty()) {
            optionalExchangeRate = getCrossExchangeRate(from, to);
        }

        return optionalExchangeRate;
    }

    private Optional<ExchangeRate> getDirectExchangeRate(String from, String to) throws SQLException {
        return exchangeRateDAO.getByCodes(from, to);
    }

    private Optional<ExchangeRate> getReversedExchangeRate(String from, String to) throws SQLException {
        Optional<ExchangeRate> optionalExchangeRate = exchangeRateDAO.getByCodes(to, from);
        ExchangeRate newOptionalExchangeRate = null;

        if (optionalExchangeRate.isPresent()) {
            newOptionalExchangeRate = exchangeRateDAO.save(
                    from,
                    to,
                    BigDecimal.ONE.divide(optionalExchangeRate.get().getRate(), 6, RoundingMode.HALF_UP));
        }

        return Optional.ofNullable(newOptionalExchangeRate);
    }

    private Optional<ExchangeRate> getCrossExchangeRate(String from, String to) throws SQLException {
        Optional<ExchangeRate> optionalExchangeRateUSDFrom = exchangeRateDAO.getByCodes("USD", from);
        Optional<ExchangeRate> optionalExchangeRateUSDTo = exchangeRateDAO.getByCodes("USD", to);
        ExchangeRate exchangeRate = null;

        if (optionalExchangeRateUSDFrom.isPresent() && optionalExchangeRateUSDTo.isPresent()) {
            BigDecimal rate = optionalExchangeRateUSDTo.get().getRate().divide(
                    optionalExchangeRateUSDFrom.get().getRate(),
                    6,
                    RoundingMode.HALF_UP
            );

            exchangeRate = new ExchangeRate(
                    currencyDAO.getByCode(from).get(),
                    currencyDAO.getByCode(to).get(),
                    rate
            );
        }

        return Optional.ofNullable(exchangeRate);
    }
}
