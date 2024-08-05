package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import dao.CurrencyDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Currency;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class CurrenciesServlet extends HttpServlet {

    public void init() throws ServletException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new ServletException("Failed to load SQLite JDBC driver", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        CurrencyDAO currencyDAO = CurrencyDAO.getInstance();

        List<Currency> currencies = currencyDAO.getAll();
        String jsonCurrencies = objectMapper.writeValueAsString(currencies);
        resp.setContentType("application/json; charset=UTF-8");
        resp.setStatus(200);
        PrintWriter out = resp.getWriter();
        out.write(jsonCurrencies);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");
        CurrencyDAO currencyDAO = CurrencyDAO.getInstance();

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
