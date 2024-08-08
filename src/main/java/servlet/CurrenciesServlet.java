package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import dao.CurrencyDAO;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Currency;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class CurrenciesServlet extends HttpServlet {
    private final CurrencyDAO currencyDAO = CurrencyDAO.getInstance();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        List<Currency> currencies = currencyDAO.getAll();
        String jsonCurrencies = objectMapper.writeValueAsString(currencies);
        resp.setStatus(200);
        PrintWriter out = resp.getWriter();
        out.write(jsonCurrencies);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        Currency currency = new Currency(
                0,
                req.getParameter("code"),
                req.getParameter("name"),
                req.getParameter("sign")
        );

        currencyDAO.save(currency);
        resp.sendRedirect(req.getContextPath() + "/currency/" + req.getParameter("code"));
    }
}
