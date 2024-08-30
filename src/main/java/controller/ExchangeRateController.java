package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dao.ExchangeRateDAO;
import dto.ExchangeRateReqDTO;
import dto.ExchangeRateRespDTO;
import dto.Error;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;

import static jakarta.servlet.http.HttpServletResponse.*;

public class ExchangeRateController extends HttpServlet {
    private final ExchangeRateDAO exchangeRateDAO = ExchangeRateDAO.getInstance();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String currencyPair = req.getPathInfo().replaceAll("/", "");

        if (!isCurrencyPairComplete(currencyPair)) {
            resp.setStatus(SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new Error(
                    SC_BAD_REQUEST,
                    "There is no code for one or two currencies."
            ));

            return;
        }

        String baseCurrencyCode = currencyPair.substring(0, 3);
        String targetCurrencyCode = currencyPair.substring(3, 6);

        ExchangeRateReqDTO exchangeRateReqDTO = new ExchangeRateReqDTO();
        exchangeRateReqDTO.setBaseCurrencyCode(baseCurrencyCode);
        exchangeRateReqDTO.setTargetCurrencyCode(targetCurrencyCode);

        try {
            Optional<ExchangeRateRespDTO> optionalExRateRespDTO = exchangeRateDAO.getByCodes(exchangeRateReqDTO);

            if (optionalExRateRespDTO.isEmpty()) {
                resp.setStatus(SC_NOT_FOUND);
                objectMapper.writeValue(resp.getWriter(), new Error(
                        SC_NOT_FOUND,
                        "The requested exchange rate was not found."
                ));

                return;
            }

            objectMapper.writeValue(resp.getWriter(), optionalExRateRespDTO.get());
        } catch (SQLException e) {
            objectMapper.writeValue(resp.getWriter(), new Error(
                    SC_INTERNAL_SERVER_ERROR,
                    "The database is unavailable. Please try again later."
            ));
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String method = req.getMethod();

        if ("PATCH".equalsIgnoreCase(method)) {
            doPatch(req, resp);
        } else {
            super.service(req, resp);
        }
    }

    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String currencyPair = req.getPathInfo().replaceAll("/", "");

        Optional<BigDecimal> optionalRate = getOptionalRateParameter(req);

        if (optionalRate.isEmpty()) {
            resp.setStatus(SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new Error(
                    SC_BAD_REQUEST,
                    "The rate is empty."
            ));

            return;
        }

        if (BigDecimal.ZERO.equals(optionalRate.get())) {
            resp.setStatus(SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new Error(
                    SC_BAD_REQUEST,
                    "The rate equals zero."
            ));

            return;
        }

        if (!isCurrencyPairComplete(currencyPair)) {
            resp.setStatus(SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new Error(
                    SC_BAD_REQUEST,
                    "There is no code for one or two currencies."
            ));

            return;
        }

        String baseCurrencyCode = currencyPair.substring(0, 3);
        String targetCurrencyCode = currencyPair.substring(3, 6);
        ExchangeRateReqDTO exRateReqDTO = new ExchangeRateReqDTO();

        exRateReqDTO.setBaseCurrencyCode(baseCurrencyCode);
        exRateReqDTO.setTargetCurrencyCode(targetCurrencyCode);

        try {

            Optional<ExchangeRateRespDTO> optionalExRateRespDTO = exchangeRateDAO.getByCodes(exRateReqDTO);

            if (optionalExRateRespDTO.isEmpty()) {
                resp.setStatus(SC_NOT_FOUND);
                objectMapper.writeValue(resp.getWriter(), new Error(
                        SC_NOT_FOUND,
                        "The requested exchange rate was not found."
                ));

                return;
            }

            ExchangeRateRespDTO exRateRespDTO = optionalExRateRespDTO.get();
            exRateRespDTO.setRate(optionalRate.get());

            if (exchangeRateDAO.update(exRateRespDTO)) {
                objectMapper.writeValue(resp.getWriter(), exRateRespDTO);
            }

        } catch (SQLException e) {
            objectMapper.writeValue(resp.getWriter(), new Error(
                    SC_INTERNAL_SERVER_ERROR,
                    "The database is unavailable. Please try again later."
            ));
        }
    }

    private static Optional<BigDecimal> getOptionalRateParameter(HttpServletRequest req) throws IOException {
        String stringRate = "";
        String line;
        BigDecimal rate = null;

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

        return Optional.ofNullable(rate);
    }

    private boolean isCurrencyPairComplete(String currencyPair) {
        return !currencyPair.isBlank() && currencyPair.length() >= 6;
    }
}
