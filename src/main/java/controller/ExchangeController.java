package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dto.ExchangeReqDTO;
import dto.ExchangeRespDTO;
import dto.Error;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.ExchangeService;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;

import static jakarta.servlet.http.HttpServletResponse.*;

public class ExchangeController extends HttpServlet {
    private final ExchangeService exchangeService = new ExchangeService();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String from = req.getParameter("from");
        String to = req.getParameter("to");
        BigDecimal amount = new BigDecimal(req.getParameter("amount"));

        ExchangeReqDTO exchangeReqDTO = new ExchangeReqDTO(from, to, amount);

        if (from.isEmpty() || from.isBlank()) {
            resp.setStatus(SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new Error(
                    SC_BAD_REQUEST,
                    "The code of base currency is empty."
            ));
        }

        if (to.isEmpty() || to.isBlank()) {
            resp.setStatus(SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new Error(
                    SC_BAD_REQUEST,
                    "The code of target currency is empty."
            ));
        }

        if (String.valueOf(amount).isEmpty() || String.valueOf(amount).isBlank()) {
            resp.setStatus(SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new Error(
                    SC_BAD_REQUEST,
                    "The amount to be exchanged is empty."
            ));
        }

        if (BigDecimal.ZERO.equals(amount)) {
            resp.setStatus(SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new Error(
                    SC_BAD_REQUEST,
                    "The amount to be exchanged equals zero."
            ));
        }

        try {
            Optional<ExchangeRespDTO> exchange = exchangeService.makeExchange(exchangeReqDTO);

            if (exchange.isPresent()) {
                objectMapper.writeValue(resp.getWriter(), exchange.get());
            } else {
                resp.setStatus(SC_NOT_FOUND);
                objectMapper.writeValue(resp.getWriter(), new Error(
                        SC_NOT_FOUND,
                        "There is no exchange rate for the requested currencies."
                ));
            }
        } catch (SQLException e) {
            objectMapper.writeValue(resp.getWriter(), new Error(
                    SC_INTERNAL_SERVER_ERROR,
                    "The database is unavailable. Please try again later."
            ));
        }
    }
}
