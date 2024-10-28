package controller;

import dto.ExchangeRateReqDTO;
import entity.ExchangeRate;
import exception.InvalidParamException;
import exception.InvalidRequestException;
import exception.RestErrorException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.ExchangeRateService;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

import static jakarta.servlet.http.HttpServletResponse.*;

public class ExchangeRatesController extends AbstractMainController {
    private final ExchangeRateService exchangeRateService = new ExchangeRateService();

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
        BigDecimal rate = getRate(req);

        if (baseCurrencyCode.equals(targetCurrencyCode)) {
            throw new RestErrorException(SC_BAD_REQUEST, "The base and target currencies should be different");
        }

        ExchangeRateReqDTO exRateReqDTO = new ExchangeRateReqDTO(baseCurrencyCode, targetCurrencyCode, rate);

        if (!isParametersValid(exRateReqDTO)) {
            throw new InvalidParamException();
        }

        if (exRateReqDTO.getRate().compareTo(ExchangeRateReqDTO.getMinPositiveRate()) < 0) {
            throw new RestErrorException(SC_BAD_REQUEST, "The rate must be at least " + ExchangeRateReqDTO.getMinPositiveRate());
        }

        ExchangeRate exchangeRate = exchangeRateService.preserve(exRateReqDTO)
                .orElseThrow(() -> new RestErrorException(SC_INTERNAL_SERVER_ERROR, "Something happened with the database. Please try again later!"));

        updateReversedExchangeRate(exRateReqDTO);

        resp.setStatus(SC_CREATED);
        objectMapper.writeValue(resp.getWriter(), exchangeRate);
    }

    private BigDecimal getRate(HttpServletRequest req) {
        String stringRate = req.getParameter("rate").replaceAll(",", ".");

        if (stringRate.isBlank()) {
            stringRate = String.valueOf(0);
        }

        return new BigDecimal(stringRate);
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
