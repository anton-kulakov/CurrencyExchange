package servlet;

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
import java.util.List;

public class ExchangeRatesServlet extends HttpServlet {
    private final ExchangeRateDAO exchangeRateDAO = ExchangeRateDAO.getInstance();
    private final CurrencyDAO currencyDAO = CurrencyDAO.getInstance();
    private final ObjectMapper objectMapper = new ObjectMapper();
    public void init() throws ServletException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new ServletException("Failed to load SQLite JDBC driver", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        List<ExchangeRate> exchangeRates = exchangeRateDAO.getAll();
        String jsonExchangeRates = objectMapper.writeValueAsString(exchangeRates);
        resp.setContentType("application/json; charset=UTF-8");
        resp.setStatus(200);
        PrintWriter out = resp.getWriter();
        out.write(jsonExchangeRates);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");

        String baseCurrencyCode = req.getParameter("baseCurrencyCode");
        String targetCurrencyCode = req.getParameter("targetCurrencyCode");
        BigDecimal rate = new BigDecimal(req.getParameter("rate"));

        exchangeRateDAO.save(
                new ExchangeRate(
                        0,
                        currencyDAO.getByCode(baseCurrencyCode).get(),
                        currencyDAO.getByCode(targetCurrencyCode).get(),
                        rate
                )
        );

        resp.sendRedirect(req.getContextPath() + "/exchangeRate/" + baseCurrencyCode + targetCurrencyCode);
    }
}
