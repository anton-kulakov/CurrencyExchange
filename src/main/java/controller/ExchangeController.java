package controller;

import dto.ExchangeReqDTO;
import dto.ExchangeRespDTO;
import exception.InvalidParamException;
import exception.RestErrorException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.ExchangeService;

import java.math.BigDecimal;
import java.util.Optional;

import static jakarta.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static jakarta.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static utils.CurrencyCodesValidator.isCurrencyCodeValid;

public class ExchangeController extends AbstractMainController {
    private final ExchangeService exchangeService = new ExchangeService();

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String from = req.getParameter("from");
        String to = req.getParameter("to");
        String stringAmount = req.getParameter("amount").replaceAll(",", ".");

        if (stringAmount.isBlank()) {
            stringAmount = String.valueOf(0);
        }

        BigDecimal amount = new BigDecimal(stringAmount);
        ExchangeReqDTO exchangeReqDTO = new ExchangeReqDTO(from, to, amount);

        if (!isParametersValid(exchangeReqDTO)) {
            throw new InvalidParamException(
                    SC_BAD_REQUEST,
                    "One or more parameters are not valid"
            );
        }

        Optional<ExchangeRespDTO> optionalExchangeRespDTO = exchangeService.makeExchange(exchangeReqDTO);

        if (optionalExchangeRespDTO.isEmpty()) {
            throw new RestErrorException(
                    SC_INTERNAL_SERVER_ERROR,
                    "Something happened with the database. Please try again later!"
            );
        }

        objectMapper.writeValue(resp.getWriter(), optionalExchangeRespDTO.get());
    }

    private boolean isParametersValid(ExchangeReqDTO exchangeReqDTO) {
        return !exchangeReqDTO.getFrom().isBlank() &&
               !exchangeReqDTO.getTo().isBlank() &&
               isCurrencyCodeValid(exchangeReqDTO.getFrom()) &&
               isCurrencyCodeValid(exchangeReqDTO.getTo()) &&
               !BigDecimal.ZERO.equals(exchangeReqDTO.getAmount());
    }
}
