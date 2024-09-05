package controller;

import dto.ExchangeRateReqDTO;
import dto.ExchangeRateRespDTO;
import dto.RestErrorException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.Optional;

import static jakarta.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static utils.CurrencyCodesValidator.isCurrencyCodeValid;

public class ExchangeRateController extends AbstractMainController {

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        if (req.getPathInfo().isBlank() || req.getPathInfo().replaceAll("[^a-zA-Z]", "").length() != 6) {
            throw new RestErrorException(
                    SC_BAD_REQUEST,
                    "The request is not valid"
            );
        }

        ExchangeRateReqDTO exRateReqDTO = getExRateReqDTO(req);

        if (!isCurrencyCodesValid(exRateReqDTO)) {
            throw new RestErrorException(
                    SC_BAD_REQUEST,
                    "One or more parameters are not valid"
            );
        }

        Optional<ExchangeRateRespDTO> optionalExRateRespDTO = exchangeRateDAO.getByCodes(exRateReqDTO);

        if (optionalExRateRespDTO.isEmpty()) {
            throw new SQLException();
        }

        objectMapper.writeValue(resp.getWriter(), optionalExRateRespDTO.get());
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
            throw new RestErrorException(
                    SC_BAD_REQUEST,
                    "The request is not valid"
            );
        }

        ExchangeRateReqDTO exRateReqDTO = getExRateReqDTO(req);
        BigDecimal rate = getRateParameter(req);
        exRateReqDTO.setRate(rate);

        if (!isCurrencyCodesValid(exRateReqDTO) || BigDecimal.ZERO.equals(exRateReqDTO.getRate())) {
            throw new RestErrorException(
                    SC_BAD_REQUEST,
                    "One or more parameters are not valid"
            );
        }

        Optional<ExchangeRateRespDTO> optionalExRateRespDTO = exchangeRateDAO.getByCodes(exRateReqDTO);

        if (optionalExRateRespDTO.isEmpty()) {
            throw new SQLException();
        }

        ExchangeRateRespDTO exRateRespDTO = optionalExRateRespDTO.get();
        exRateRespDTO.setRate(exRateReqDTO.getRate());

        updateReversedExchangeRate(exRateReqDTO);

        if (exchangeRateDAO.update(exRateRespDTO)) {
            objectMapper.writeValue(resp.getWriter(), exRateRespDTO);
        } else {
            throw new SQLException();
        }
    }

    private boolean isCurrencyCodesValid(ExchangeRateReqDTO exRateReqDTO) {
        return isCurrencyCodeValid(exRateReqDTO.getBaseCurrencyCode()) &&
               isCurrencyCodeValid(exRateReqDTO.getTargetCurrencyCode());
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

            if (keyValue[0].equals("rate")) {
                stringRate = keyValue[1];
            }
        }

        if (!stringRate.isEmpty() || !stringRate.isBlank()) {
            rate = new BigDecimal(stringRate);
        }

        return rate;
    }
}
