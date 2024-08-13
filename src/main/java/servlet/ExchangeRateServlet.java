package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import dao.ExchangeRateDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Error;
import model.ExchangeRate;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;

import static jakarta.servlet.http.HttpServletResponse.*;

public class ExchangeRateServlet extends HttpServlet {
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

        try {
            Optional<ExchangeRate> optionalExchangeRate = exchangeRateDAO.getByCode(baseCurrencyCode, targetCurrencyCode);

            if (optionalExchangeRate.isEmpty()) {
                resp.setStatus(SC_NOT_FOUND);
                objectMapper.writeValue(resp.getWriter(), new Error(
                        SC_NOT_FOUND,
                        "The requested exchange rate was not found."
                ));

                return;
            }

            objectMapper.writeValue(resp.getWriter(), optionalExchangeRate.get());
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

        try {
            Optional<ExchangeRate> optionalExchangeRate = exchangeRateDAO.getByCode(baseCurrencyCode, targetCurrencyCode);

            if (optionalExchangeRate.isEmpty()) {
                resp.setStatus(SC_NOT_FOUND);
                objectMapper.writeValue(resp.getWriter(), new Error(
                        SC_NOT_FOUND,
                        "The requested exchange rate was not found."
                ));

                return;
            }

            ExchangeRate exchangeRate = optionalExchangeRate.get();
            exchangeRate.setRate(new BigDecimal(req.getParameter("rate")));

            if (exchangeRateDAO.update(exchangeRate)) {
                objectMapper.writeValue(resp.getWriter(), exchangeRate);
            }

        } catch (SQLException e) {
            objectMapper.writeValue(resp.getWriter(), new Error(
                    SC_INTERNAL_SERVER_ERROR,
                    "The database is unavailable. Please try again later."
            ));
        }
    }
    private boolean isCurrencyPairComplete(String currencyPair) {
        return !currencyPair.isBlank() && currencyPair.length() >= 6;
    }
}
