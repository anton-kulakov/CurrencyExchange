package controller;

import entity.Currency;
import exception.InvalidParamException;
import exception.RestErrorException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static jakarta.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static jakarta.servlet.http.HttpServletResponse.SC_NOT_FOUND;

public class CurrencyController extends AbstractMainController {
    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String currencyCode = req.getPathInfo().replaceAll("/", "");

        if (currencyCode.isBlank()) {
            throw new InvalidParamException(SC_BAD_REQUEST, "One or more parameters are not valid");
        }

        if (!isCurrencyCodeFollowStandard(currencyCode)) {
            throw new RestErrorException(SC_BAD_REQUEST, "The currency code must follow the ISO 4217 standard");
        }

        Currency currency = currencyDAO.getByCode(currencyCode)
                .orElseThrow(() -> new RestErrorException(SC_NOT_FOUND, "The requested currency was not found"));

        objectMapper.writeValue(resp.getWriter(), currency);
    }
}
