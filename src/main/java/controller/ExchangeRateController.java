package controller;

import dto.ExchangeRateReqDTO;
import entity.ExchangeRate;
import exception.InvalidParamException;
import exception.InvalidRequestException;
import exception.RestErrorException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;

import static jakarta.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static jakarta.servlet.http.HttpServletResponse.SC_NOT_FOUND;

public class ExchangeRateController extends AbstractMainController {

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        if (req.getPathInfo().isBlank() || req.getPathInfo().replaceAll("[^a-zA-Z]", "").length() != 6) {
            throw new InvalidRequestException();
        }

        ExchangeRateReqDTO exRateReqDTO = getExRateReqDTO(req);

        if (!isCurrencyPairFollowStandard(exRateReqDTO)) {
            throw new InvalidParamException();
        }

        ExchangeRate exchangeRate = exchangeRateDAO.getByCodes(exRateReqDTO.getBaseCurrencyCode(), exRateReqDTO.getTargetCurrencyCode())
                .orElseThrow(() -> new RestErrorException(SC_NOT_FOUND, "The requested exchange rate could not be found in the database"));

        objectMapper.writeValue(resp.getWriter(), exchangeRate);
    }

    private static ExchangeRateReqDTO getExRateReqDTO(HttpServletRequest req) {
        String currencyPair = req.getPathInfo().substring(1, 7);
        String baseCurrencyCode = currencyPair.substring(0, 3);
        String targetCurrencyCode = currencyPair.substring(3, 6);

        ExchangeRateReqDTO exRateReqDTO = new ExchangeRateReqDTO();
        exRateReqDTO.setBaseCurrencyCode(baseCurrencyCode);
        exRateReqDTO.setTargetCurrencyCode(targetCurrencyCode);

        return exRateReqDTO;
    }

    @Override
    protected void handlePatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        if (req.getPathInfo().isBlank() || req.getPathInfo().replaceAll("[^a-zA-Z]", "").length() != 6) {
            throw new InvalidRequestException();
        }

        ExchangeRateReqDTO exRateReqDTO = getExRateReqDTO(req);
        BigDecimal rate = getRateParameter(req);
        exRateReqDTO.setRate(rate);

        if (!isCurrencyPairFollowStandard(exRateReqDTO) || BigDecimal.ZERO.equals(exRateReqDTO.getRate())) {
            throw new InvalidParamException();
        }

        if (exRateReqDTO.getRate().compareTo(ExchangeRateReqDTO.getMinPositiveRate()) < 0) {
            throw new RestErrorException(SC_BAD_REQUEST, "The rate must be at least " + ExchangeRateReqDTO.getMinPositiveRate());
        }

        ExchangeRate exchangeRate = exchangeRateDAO.getByCodes(exRateReqDTO.getBaseCurrencyCode(), exRateReqDTO.getTargetCurrencyCode())
                .orElseThrow(() -> new RestErrorException(SC_NOT_FOUND, "The requested exchange rate could not be found in the database"));

        exchangeRate.setRate(exRateReqDTO.getRate());
        updateReversedExchangeRate(exRateReqDTO);

        if (exchangeRateDAO.update(exchangeRate)) {
            objectMapper.writeValue(resp.getWriter(), exchangeRate);
        }
    }

    private boolean isCurrencyPairFollowStandard(ExchangeRateReqDTO exRateReqDTO) {
        return isCurrencyCodeFollowStandard(exRateReqDTO.getBaseCurrencyCode()) &&
               isCurrencyCodeFollowStandard(exRateReqDTO.getTargetCurrencyCode());
    }

    private static BigDecimal getRateParameter(HttpServletRequest req) throws IOException {
        String stringRate = "";
        String line;
        BigDecimal rate = BigDecimal.ZERO;

        BufferedReader reader = req.getReader();
        StringBuilder formBody = new StringBuilder();

        while ((line = reader.readLine()) != null) {
            formBody.append(line);
        }

        String[] params = formBody.toString().split("&");

        for (String param : params) {

            String[] keyValue = param.split("=");

            if (keyValue.length == 2 && keyValue[0].equals("rate")) {
                stringRate = keyValue[1];
            }
        }

        if (!stringRate.isEmpty() || !stringRate.isBlank()) {
            rate = new BigDecimal(stringRate.replaceAll(",", "."));
        }

        return rate;
    }
}
