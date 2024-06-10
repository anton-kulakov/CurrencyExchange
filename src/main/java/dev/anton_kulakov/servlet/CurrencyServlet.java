package dev.anton_kulakov.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Currency;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

public class CurrencyServlet extends HttpServlet {
    private static final String URL = "jdbc:sqlite:C:/Users/anton/IdeaProjects/CurrencyExchange/src/main/resources/database.db";

    public void init() throws ServletException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new ServletException("Failed to load SQLite JDBC driver", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        String[] pathParts = pathInfo.split("/");
        String currencyCode = pathParts[1];

        Currency currency = new Currency();
        ObjectMapper objectMapper = new ObjectMapper();
        String SQLQuery = "SELECT * FROM Currencies WHERE Code = ?";

        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement statement = connection.prepareStatement(SQLQuery)) {

            statement.setString(1, currencyCode);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    currency.setId(resultSet.getInt("id"));
                    currency.setCode(resultSet.getString("code"));
                    currency.setFullName(resultSet.getString("fullname"));
                    currency.setSign(resultSet.getString("sign"));
                }
            }
        } catch (SQLException e) {
            resp.setStatus(500);
        }

        String jsonCurrency = objectMapper.writeValueAsString(currency);
        resp.setContentType("application/json; charset=UTF-8");
        resp.setStatus(200);
        PrintWriter out = resp.getWriter();
        out.write(jsonCurrency);
    }
}
