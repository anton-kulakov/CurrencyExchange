package controller;

import dto.ExchangeReqDTO;
import dto.ExchangeRespDTO;
import exception.InvalidParamException;
import exception.InvalidRequestException;
import exception.RestErrorException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.ExchangeService;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

import static jakarta.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static jakarta.servlet.http.HttpServletResponse.SC_NOT_FOUND;

public class ExchangeController extends AbstractMainController {
    private final ExchangeService exchangeService = new ExchangeService();

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        if (!isRequestValid(req.getParameterMap())) {
            throw new InvalidRequestException(SC_BAD_REQUEST, "The request isn't valid");
        }

        String from = req.getParameter("from");
        String to = req.getParameter("to");
        BigDecimal amount = getAmount(req);

        ExchangeReqDTO exchangeReqDTO = new ExchangeReqDTO(from, to, amount);

        if (!isParametersValid(exchangeReqDTO)) {
            throw new InvalidParamException(SC_BAD_REQUEST, "One or more parameters are not valid");
        }

        if (exchangeReqDTO.getAmount().compareTo(ExchangeReqDTO.getMinPositiveAmount()) < 0) {
            throw new RestErrorException(SC_BAD_REQUEST, "The amount must be at least " + ExchangeReqDTO.getMinPositiveAmount());
        }

        ExchangeRespDTO exchangeRespDTO = exchangeService.makeExchange(exchangeReqDTO)
                .orElseThrow(() -> new RestErrorException(SC_NOT_FOUND, "There is no exchange rate available for the requested currencies"));

        objectMapper.writeValue(resp.getWriter(), exchangeRespDTO);
    }

    private boolean isRequestValid(Map<String, String[]> parameterMap) {
        Set<String> requiredParams = Set.of("from", "to", "amount");

        return parameterMap.keySet().containsAll(requiredParams);
    }

    private BigDecimal getAmount(HttpServletRequest req) {
        String stringAmount = req.getParameter("amount").replaceAll(",", ".");

        if (stringAmount.isBlank()) {
            stringAmount = String.valueOf(0);
        }

        return new BigDecimal(stringAmount);
    }

    private boolean isParametersValid(ExchangeReqDTO exchangeReqDTO) {
        return !exchangeReqDTO.getFrom().isBlank() &&
               !exchangeReqDTO.getTo().isBlank() &&
               isCurrencyCodeFollowStandard(exchangeReqDTO.getFrom()) &&
               isCurrencyCodeFollowStandard(exchangeReqDTO.getTo()) &&
               !BigDecimal.ZERO.equals(exchangeReqDTO.getAmount());
    }
}
