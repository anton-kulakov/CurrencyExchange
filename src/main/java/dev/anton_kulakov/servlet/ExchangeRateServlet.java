package dev.anton_kulakov.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Currency;
import model.ExchangeRate;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

public class ExchangeRateServlet extends HttpServlet {
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
        String currencyPair = pathParts[1];
        String baseCurrencyCode = currencyPair.substring(0, 3);
        String targetCurrencyCode = currencyPair.substring(3);

        ExchangeRate exchangeRate = new ExchangeRate();
        ObjectMapper objectMapper = new ObjectMapper();
        String SQLQuery = "SELECT\n" +
                "    er.id,\n" +
                "    bc.id AS BaseCurrencyID,\n" +
                "    bc.code AS BaseCurrencyCode,\n" +
                "    bc.fullname AS BaseCurrencyFullName,\n" +
                "    bc.sign AS BaseCurrencySign,\n" +
                "    tc.id AS TargetCurrencyID,\n" +
                "    tc.code AS TargetCurrencyCode,\n" +
                "    tc.fullname AS TargetCurrencyFullName,\n" +
                "    tc.sign AS TargetCurrencySign,\n" +
                "    er.rate\n" +
                "FROM ExchangeRates er\n" +
                "         INNER JOIN Currencies bc ON er.BaseCurrencyId = bc.id\n" +
                "         INNER JOIN Currencies tc ON er.TargetCurrencyId = tc.id\n" +
                "WHERE bc.Code = ? AND tc.Code = ?;";

        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement statement = connection.prepareStatement(SQLQuery)) {

            statement.setString(1, baseCurrencyCode);
            statement.setString(2, targetCurrencyCode);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Currency baseCurrency = new Currency();
                    Currency targetCurrency = new Currency();

                    baseCurrency.setId(resultSet.getInt("BaseCurrencyID"));
                    baseCurrency.setCode(resultSet.getString("BaseCurrencyCode"));
                    baseCurrency.setFullName(resultSet.getString("BaseCurrencyFullName"));
                    baseCurrency.setSign(resultSet.getString("BaseCurrencySign"));

                    targetCurrency.setId(resultSet.getInt("TargetCurrencyID"));
                    targetCurrency.setCode(resultSet.getString("TargetCurrencyCode"));
                    targetCurrency.setFullName(resultSet.getString("TargetCurrencyFullName"));
                    targetCurrency.setSign(resultSet.getString("TargetCurrencySign"));

                    exchangeRate.setId(resultSet.getInt("id"));
                    exchangeRate.setBaseCurrency(baseCurrency);
                    exchangeRate.setTargetCurrency(targetCurrency);
                    exchangeRate.setRate(resultSet.getDouble("rate"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        String jsonExchangeRate = objectMapper.writeValueAsString(exchangeRate);
        resp.setContentType("application/json; charset=UTF-8");
        resp.setStatus(200);
        PrintWriter out = resp.getWriter();
        out.write(jsonExchangeRate);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String method = req.getMethod();

        if("PATCH".equalsIgnoreCase(method)) {
            doPatch(req, resp);
        } else {
            super.service(req, resp);
        }
    }

    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");

        String pathInfo = req.getPathInfo();
        String[] pathParts = pathInfo.split("/");
        String currencyPair = pathParts[1];
        String baseCurrencyCode = currencyPair.substring(0, 3);
        String targetCurrencyCode = currencyPair.substring(3, 6);
        double rate = Double.parseDouble(req.getParameter("rate"));

        String currencyIDQuery = "SELECT id FROM Currencies WHERE Code = ?";
        String SQLQuery = "UPDATE ExchangeRates SET Rate = ? WHERE BaseCurrencyID = ? " +
                "AND TargetCurrencyID = ?";

        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement baseCurrencyStatement = connection.prepareStatement(currencyIDQuery);
             PreparedStatement targetCurrencyStatement = connection.prepareStatement(currencyIDQuery);
             PreparedStatement updateExchangeRateStatement = connection.prepareStatement(SQLQuery)) {

            baseCurrencyStatement.setString(1, baseCurrencyCode);
            targetCurrencyStatement.setString(1, targetCurrencyCode);

            int baseCurrencyID = 0;
            int targetCurrencyID = 0;

            try (ResultSet resultSet = baseCurrencyStatement.executeQuery()) {
                while (resultSet.next()) {
                    baseCurrencyID = resultSet.getInt("ID");
                }
            }

            try (ResultSet resultSet = targetCurrencyStatement.executeQuery()) {
                while (resultSet.next()) {
                    targetCurrencyID = resultSet.getInt("ID");
                }
            }

            updateExchangeRateStatement.setDouble(1, rate);
            updateExchangeRateStatement.setInt(2, baseCurrencyID);
            updateExchangeRateStatement.setInt(3, targetCurrencyID);

            updateExchangeRateStatement.executeUpdate();

            doGet(req, resp);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
