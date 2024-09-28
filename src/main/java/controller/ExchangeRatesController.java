package controller;

import dto.CurrencyDTO;
import dto.ExchangeRateReqDTO;
import dto.ExchangeRateRespDTO;
import exception.InvalidParamException;
import exception.InvalidRequestException;
import exception.RestErrorException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static jakarta.servlet.http.HttpServletResponse.*;

public class ExchangeRatesController extends AbstractMainController {
    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        objectMapper.writeValue(resp.getWriter(), exchangeRateDAO.getAll());
    }

    @Override
    protected void handlePost(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        if (!isRequestValid(req.getParameterMap())) {
            throw new InvalidRequestException();
        }

        String baseCurrencyCode = req.getParameter("baseCurrencyCode");
        String targetCurrencyCode = req.getParameter("targetCurrencyCode");
        String stringRate = req.getParameter("rate");

        if (stringRate.isBlank()) {
            stringRate = String.valueOf(0);
        }

        if (baseCurrencyCode.equals(targetCurrencyCode)) {
            throw new RestErrorException(
                    SC_BAD_REQUEST,
                    "The base and target currencies should be different"
            );
        }

        BigDecimal rate = new BigDecimal(stringRate);

        ExchangeRateReqDTO exRateReqDTO = new ExchangeRateReqDTO(baseCurrencyCode, targetCurrencyCode, rate);

        if (!isParametersValid(exRateReqDTO)) {
            throw new InvalidParamException();
        }

        if (exRateReqDTO.getRate().compareTo(ExchangeRateReqDTO.getMinPositiveRate()) < 0) {
            throw new RestErrorException(
                    SC_BAD_REQUEST,
                    "The rate must be at least " + ExchangeRateReqDTO.getMinPositiveRate()
            );
        }

        CurrencyDTO baseCurrencyDTO = new CurrencyDTO();
        CurrencyDTO targetCurrencyDTO = new CurrencyDTO();
        baseCurrencyDTO.setCode(baseCurrencyCode);
        targetCurrencyDTO.setCode(targetCurrencyCode);

        if (currencyDAO.getByCode(baseCurrencyDTO).isEmpty() || currencyDAO.getByCode(targetCurrencyDTO).isEmpty()) {
            throw new RestErrorException(
                    SC_NOT_FOUND,
                    "One or both currencies from a currency pair do not exist"
            );
        }

        if (exchangeRateDAO.getByCodes(exRateReqDTO).isPresent()) {
            throw new RestErrorException(
                    SC_CONFLICT,
                    "This exchange rate already exists"
            );
        }

        Optional<ExchangeRateRespDTO> optExRateRespDTO = exchangeRateDAO.save(exRateReqDTO);

        if (optExRateRespDTO.isEmpty()) {
            throw new RestErrorException(
                    SC_INTERNAL_SERVER_ERROR,
                    "Something happened with the database. Please try again later!"
            );
        }

        updateReversedExchangeRate(exRateReqDTO);

        resp.setStatus(SC_CREATED);
        objectMapper.writeValue(resp.getWriter(), optExRateRespDTO.get());
    }

    private boolean isRequestValid(Map<String, String[]> parameterMap) {
        Set<String> requiredParams = Set.of("baseCurrencyCode", "targetCurrencyCode", "rate");

        return parameterMap.keySet().containsAll(requiredParams);
    }

    private boolean isParametersValid(ExchangeRateReqDTO exRateReqDTO) {
        return !exRateReqDTO.getBaseCurrencyCode().isBlank() &&
               !exRateReqDTO.getTargetCurrencyCode().isBlank() &&
               isCurrencyCodeFollowStandard(exRateReqDTO.getBaseCurrencyCode()) &&
               isCurrencyCodeFollowStandard(exRateReqDTO.getTargetCurrencyCode()) &&
               !BigDecimal.ZERO.equals(exRateReqDTO.getRate());
    }
}
