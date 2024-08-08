package servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import dao.CurrencyDAO;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Currency;

import java.io.IOException;
import java.io.PrintWriter;

public class CurrencyServlet extends HttpServlet {
    private final CurrencyDAO currencyDAO = CurrencyDAO.getInstance();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        String[] pathParts = pathInfo.split("/");
        String currencyCode = pathParts[1];

        Currency currency = currencyDAO.getByCode(currencyCode).get();

        String jsonCurrency = objectMapper.writeValueAsString(currency);
        resp.setStatus(200);
        PrintWriter out = resp.getWriter();
        out.write(jsonCurrency);
    }
}
