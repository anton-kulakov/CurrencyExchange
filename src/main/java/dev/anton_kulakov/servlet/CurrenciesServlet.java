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
import java.util.ArrayList;
import java.util.List;

public class CurrenciesServlet extends HttpServlet {
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
        List<Currency> currencyList = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();

        try (Connection connection = DriverManager.getConnection(URL)) {
            String SQLQuery = "SELECT * FROM Currencies";

            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(SQLQuery)) {

                while (resultSet.next()) {
                    Currency currency = new Currency();

                    currency.setId(resultSet.getInt("id"));
                    currency.setCode(resultSet.getString("code"));
                    currency.setFullName(resultSet.getString("fullname"));
                    currency.setSign(resultSet.getString("sign"));

                    currencyList.add(currency);
                }
            }
        } catch (SQLException e) {
            resp.setStatus(500);
        }

        String jsonCurrencies = objectMapper.writeValueAsString(currencyList);
        resp.setContentType("application/json; charset=UTF-8");
        resp.setStatus(200);
        PrintWriter out = resp.getWriter();
        out.write(jsonCurrencies);
    }
}
