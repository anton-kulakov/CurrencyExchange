package dev.anton_kulakov.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import dao.CurrencyDAO;
import dao.ExchangeRateDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.ExchangeRate;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;

public class ExchangeRateServlet extends HttpServlet {
    public void init() throws ServletException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new ServletException("Failed to load SQLite JDBC driver", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        String[] pathParts = pathInfo.split("/");
        String currencyPair = pathParts[1];
        String baseCurrencyCode = currencyPair.substring(0, 3);
        String targetCurrencyCode = currencyPair.substring(3);

        CurrencyDAO currencyDAO = CurrencyDAO.getInstance();
        ExchangeRateDAO exchangeRateDAO = ExchangeRateDAO.getInstance();
        ObjectMapper objectMapper = new ObjectMapper();

        ExchangeRate exchangeRate = exchangeRateDAO.getByCode(new ExchangeRate(
                0,
                currencyDAO.getByCode(baseCurrencyCode).get(),
                currencyDAO.getByCode(targetCurrencyCode).get(),
                new BigDecimal(0)
        ));

        String jsonExchangeRate = objectMapper.writeValueAsString(exchangeRate);
        resp.setContentType("application/json; charset=UTF-8");
        resp.setStatus(200);
        PrintWriter out = resp.getWriter();
        out.write(jsonExchangeRate);
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
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");

        String pathInfo = req.getPathInfo();
        String[] pathParts = pathInfo.split("/");
        String currencyPair = pathParts[1];
        String baseCurrencyCode = currencyPair.substring(0, 3);
        String targetCurrencyCode = currencyPair.substring(3, 6);

        CurrencyDAO currencyDAO = CurrencyDAO.getInstance();
        ExchangeRateDAO exchangeRateDAO = ExchangeRateDAO.getInstance();

        ExchangeRate exchangeRate = new ExchangeRate(
                0,
                currencyDAO.getByCode(baseCurrencyCode).get(),
                currencyDAO.getByCode(targetCurrencyCode).get(),
                new BigDecimal(req.getParameter("rate"))
        );

        exchangeRateDAO.update(exchangeRate);

        doGet(req, resp);
    }
}
