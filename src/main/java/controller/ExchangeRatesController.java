package controller;

import dto.CurrencyDTO;
import dto.ExchangeRateReqDTO;
import dto.ExchangeRateRespDTO;
import exception.InvalidParamException;
import exception.RestErrorException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.math.BigDecimal;
import java.util.Optional;

import static jakarta.servlet.http.HttpServletResponse.*;

public class ExchangeRatesController extends AbstractMainController {
    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        objectMapper.writeValue(resp.getWriter(), exchangeRateDAO.getAll());
    }

    @Override
    protected void handlePost(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String baseCurrencyCode = req.getParameter("baseCurrencyCode");
        String targetCurrencyCode = req.getParameter("targetCurrencyCode");
        String stringRate = req.getParameter("rate");

        if (stringRate.isBlank()) {
            stringRate = String.valueOf(0);
        }

        BigDecimal rate = new BigDecimal(stringRate);

        ExchangeRateReqDTO exRateReqDTO = new ExchangeRateReqDTO(baseCurrencyCode, targetCurrencyCode, rate);

        if (!isParametersValid(exRateReqDTO)) {
            throw new InvalidParamException();
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

        Optional<ExchangeRateRespDTO> exRateRespDTO = exchangeRateDAO.save(exRateReqDTO);

        if (exRateRespDTO.isEmpty()) {
            throw new RestErrorException(
                    SC_INTERNAL_SERVER_ERROR,
                    "Something happened with the database. Please try again later!"
            );
        }

        updateReversedExchangeRate(exRateReqDTO);

        resp.setStatus(SC_CREATED);
        objectMapper.writeValue(resp.getWriter(), exRateRespDTO.get());
    }

    private boolean isParametersValid(ExchangeRateReqDTO exRateReqDTO) {
        return !exRateReqDTO.getBaseCurrencyCode().isBlank() &&
               !exRateReqDTO.getTargetCurrencyCode().isBlank() &&
               isCurrencyCodeFollowStandard(exRateReqDTO.getBaseCurrencyCode()) &&
               isCurrencyCodeFollowStandard(exRateReqDTO.getTargetCurrencyCode()) &&
               !BigDecimal.ZERO.equals(exRateReqDTO.getRate());
    }
}
