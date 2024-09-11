package controller;

import dto.ExchangeRateReqDTO;
import dto.ExchangeRateRespDTO;
import exception.InvalidParamException;
import exception.InvalidRequestException;
import exception.RestErrorException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

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

        Optional<ExchangeRateRespDTO> optionalExRateRespDTO = exchangeRateDAO.getByCodes(exRateReqDTO);

        if (optionalExRateRespDTO.isEmpty()) {
            throw new RestErrorException(
                    SC_NOT_FOUND,
                    "The requested exchange rate could not be found in the database"
            );
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
            throw new InvalidRequestException();
        }

        ExchangeRateReqDTO exRateReqDTO = getExRateReqDTO(req);
        BigDecimal rate = getRateParameter(req);
        exRateReqDTO.setRate(rate);

        if (!isCurrencyPairFollowStandard(exRateReqDTO) || BigDecimal.ZERO.equals(exRateReqDTO.getRate())) {
            throw new InvalidParamException();
        }

        Optional<ExchangeRateRespDTO> optionalExRateRespDTO = exchangeRateDAO.getByCodes(exRateReqDTO);

        if (optionalExRateRespDTO.isEmpty()) {
            throw new RestErrorException(
                    SC_NOT_FOUND,
                    "The requested exchange rate could not be found in the database"
            );
        }

        ExchangeRateRespDTO exRateRespDTO = optionalExRateRespDTO.get();
        exRateRespDTO.setRate(exRateReqDTO.getRate());

        updateReversedExchangeRate(exRateReqDTO);

        if (exchangeRateDAO.update(exRateRespDTO)) {
            objectMapper.writeValue(resp.getWriter(), exRateRespDTO);
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

            if (keyValue[0].equals("rate")) {
                stringRate = keyValue[1];
            }
        }

        if (!stringRate.isEmpty() || !stringRate.isBlank()) {
            rate = new BigDecimal(stringRate.replaceAll(",", "."));
        }

        return rate;
    }
}
