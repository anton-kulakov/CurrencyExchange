package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dao.CurrencyDAO;
import dao.ExchangeRateDAO;
import dto.CurrencyDTO;
import dto.ExchangeRateReqDTO;
import dto.ExchangeRateRespDTO;
import dto.Error;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static jakarta.servlet.http.HttpServletResponse.*;

public class ExchangeRatesController extends HttpServlet {
    private final ExchangeRateDAO exchangeRateDAO = ExchangeRateDAO.getInstance();
    private final CurrencyDAO currencyDAO = CurrencyDAO.getInstance();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            List<ExchangeRateRespDTO> exchangeRates = exchangeRateDAO.getAll();
            objectMapper.writeValue(resp.getWriter(), exchangeRates);
        } catch (SQLException e) {
            resp.setStatus(SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(resp.getWriter(), new Error(
                    SC_INTERNAL_SERVER_ERROR,
                    "The database is unavailable. Please try again later."
            ));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String baseCurrencyCode = req.getParameter("baseCurrencyCode");
        String targetCurrencyCode = req.getParameter("targetCurrencyCode");
        BigDecimal rate = new BigDecimal(req.getParameter("rate"));
        CurrencyDTO baseCurrencyDTO = new CurrencyDTO();
        CurrencyDTO targetCurrencyDTO = new CurrencyDTO();

        ExchangeRateReqDTO exchangeRateReqDTO = new ExchangeRateReqDTO(baseCurrencyCode, targetCurrencyCode, rate);
        baseCurrencyDTO.setCode(baseCurrencyCode);
        targetCurrencyDTO.setCode(targetCurrencyCode);

        if (baseCurrencyCode.isEmpty() || baseCurrencyCode.isBlank()) {
            resp.setStatus(SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new Error(
                    SC_BAD_REQUEST,
                    "The code of base currency is empty."
            ));

            return;
        }

        if (targetCurrencyCode.isEmpty() || targetCurrencyCode.isBlank()) {
            resp.setStatus(SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new Error(
                    SC_BAD_REQUEST,
                    "The code of target currency is empty."
            ));

            return;
        }

        if (String.valueOf(rate).isEmpty() || String.valueOf(rate).isBlank()) {
            resp.setStatus(SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new Error(
                    SC_BAD_REQUEST,
                    "The rate is empty."
            ));

            return;
        }

        if (BigDecimal.ZERO.equals(rate)) {
            resp.setStatus(SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new Error(
                    SC_BAD_REQUEST,
                    "The rate equals zero."
            ));

            return;
        }

        try {
            if (currencyDAO.getByCode(baseCurrencyDTO).isEmpty() || currencyDAO.getByCode(targetCurrencyDTO).isEmpty()) {
                resp.setStatus(SC_NOT_FOUND);
                objectMapper.writeValue(resp.getWriter(), new Error(
                        SC_NOT_FOUND,
                        "One or both currencies from a currency pair do not exist."
                ));

                return;
            }

            if (exchangeRateDAO.getByCodes(exchangeRateReqDTO).isPresent()) {
                resp.setStatus(SC_CONFLICT);
                objectMapper.writeValue(resp.getWriter(), new Error(
                        SC_CONFLICT,
                        "This exchange rate already exists."
                ));

                return;
            }

            Optional<ExchangeRateRespDTO> exRateRespDTO = exchangeRateDAO.save(exchangeRateReqDTO);
            resp.setStatus(SC_CREATED);
            objectMapper.writeValue(resp.getWriter(), exRateRespDTO.get());
        } catch (SQLException e) {
            resp.setStatus(SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(resp.getWriter(), new Error(
                    SC_INTERNAL_SERVER_ERROR,
                    "The database is unavailable. Please try again later."
            ));
        }
    }
}
