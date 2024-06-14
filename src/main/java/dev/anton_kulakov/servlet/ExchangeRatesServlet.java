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
import java.util.ArrayList;
import java.util.List;

public class ExchangeRatesServlet extends HttpServlet {
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
        List<ExchangeRate> exchangeRateList = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();

        try (Connection connection = DriverManager.getConnection(URL)) {
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
                    "INNER JOIN Currencies bc ON er.BaseCurrencyId = bc.id\n" +
                    "INNER JOIN Currencies tc ON er.TargetCurrencyId = tc.id;";

            try(Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(SQLQuery)) {

                while (resultSet.next()) {
                    Currency baseCurrency = new Currency();
                    Currency targetCurrency = new Currency();
                    ExchangeRate exchangeRate = new ExchangeRate();

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

                    exchangeRateList.add(exchangeRate);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String jsonExchangeRates = objectMapper.writeValueAsString(exchangeRateList);
        resp.setContentType("application/json; charset=UTF-8");
        resp.setStatus(200);
        PrintWriter out = resp.getWriter();
        out.write(jsonExchangeRates);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");

        String baseCurrencyCode = req.getParameter("baseCurrencyCode");
        String targetCurrencyCode = req.getParameter("targetCurrencyCode");
        double rate = Double.parseDouble(req.getParameter("rate"));

        String currencyIDQuery = "SELECT id FROM Currencies WHERE Code = ?";
        String SQLQuery = "INSERT INTO ExchangeRates (BaseCurrencyID, TargetCurrencyID, Rate) " +
                "VALUES (?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(URL);
        PreparedStatement baseCurrencyStatement = connection.prepareStatement(currencyIDQuery);
        PreparedStatement targetCurrencyStatement = connection.prepareStatement(currencyIDQuery);
        PreparedStatement addExchangeRateStatement = connection.prepareStatement(SQLQuery)) {

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

            addExchangeRateStatement.setInt(1, baseCurrencyID);
            addExchangeRateStatement.setInt(2, targetCurrencyID);
            addExchangeRateStatement.setDouble(3, rate);

            addExchangeRateStatement.executeUpdate();

            resp.sendRedirect(req.getContextPath() + "/exchangeRate/" + baseCurrencyCode + targetCurrencyCode);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
