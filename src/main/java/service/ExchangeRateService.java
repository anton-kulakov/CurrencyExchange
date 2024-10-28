package service;

import dao.CurrencyDAO;
import dao.ExchangeRateDAO;
import dto.ExchangeRateReqDTO;
import entity.Currency;
import entity.ExchangeRate;
import exception.DBException;
import exception.RestErrorException;

import java.util.Optional;

import static jakarta.servlet.http.HttpServletResponse.SC_NOT_FOUND;

public class ExchangeRateService {
    private final CurrencyDAO currencyDAO = CurrencyDAO.getInstance();
    private final ExchangeRateDAO exchangeRateDAO = ExchangeRateDAO.getInstance();

    public Optional<ExchangeRate> preserve(ExchangeRateReqDTO exchangeRateReqDTO) throws DBException, RestErrorException {
        Currency baseCurrency = currencyDAO.getByCode(exchangeRateReqDTO.getBaseCurrencyCode())
                .orElseThrow(() -> new RestErrorException(SC_NOT_FOUND, "Currency " + exchangeRateReqDTO.getBaseCurrencyCode() + " do not exist"));
        Currency targetCurrency = currencyDAO.getByCode(exchangeRateReqDTO.getTargetCurrencyCode())
                .orElseThrow(() -> new RestErrorException(SC_NOT_FOUND, "Currency " + exchangeRateReqDTO.getTargetCurrencyCode() + " do not exist"));

        ExchangeRate exchangeRate = new ExchangeRate();
        exchangeRate.setBaseCurrency(baseCurrency);
        exchangeRate.setTargetCurrency(targetCurrency);
        exchangeRate.setRate(exchangeRateReqDTO.getRate());

        return exchangeRateDAO.save(exchangeRate);
    }
}
