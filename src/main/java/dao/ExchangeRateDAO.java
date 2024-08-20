package dao;

import model.Currency;
import model.ExchangeRate;
import utils.ConnectionManager;

import java.math.BigDecimal;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class ExchangeRateDAO {
    private final static ExchangeRateDAO INSTANCE = new ExchangeRateDAO();
    private final CurrencyDAO currencyDAO = CurrencyDAO.getInstance();
    private final static String SAVE_SQL = """
            INSERT INTO exchange_rates
            (base_currency_id, target_currency_id, rate)
            VALUES (?, ?, ?)
            """;
    private final static String GET_ALL_SQL = """
            SELECT
            er.id,
            bc.id AS base_currency_id,
            bc.code AS base_currency_code,
            bc.full_name AS base_currency_full_name,
            bc.sign AS base_currency_sign,
            tc.id AS target_currency_id,
            tc.code AS target_currency_code,
            tc.full_name AS target_currency_full_name,
            tc.sign AS target_currency_sign,
            er.rate
            FROM exchange_rates er
            INNER JOIN currencies bc ON er.base_currency_id = bc.id
            INNER JOIN currencies tc ON er.target_currency_id = tc.id
            """;
    private final static String UPDATE_SQL = """
            UPDATE exchange_rates
            SET rate = ? 
            WHERE base_currency_id = ? AND target_currency_id = ?
            """;
    private final static String GET_BY_CODE_SQL = GET_ALL_SQL + """
            WHERE bc.code = ? AND tc.code = ?
            """;

    public ExchangeRate save(String baseCurrencyCode, String targetCurrencyCode, BigDecimal rate) throws SQLException {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SAVE_SQL, Statement.RETURN_GENERATED_KEYS)) {

            ExchangeRate exchangeRate = new ExchangeRate(
                    currencyDAO.getByCode(baseCurrencyCode).get(),
                    currencyDAO.getByCode(targetCurrencyCode).get(),
                    rate
            );

            statement.setInt(1, exchangeRate.getBaseCurrency().getId());
            statement.setInt(2, exchangeRate.getTargetCurrency().getId());
            statement.setBigDecimal(3, rate);

            statement.executeUpdate();
            ResultSet generatedKeys = statement.getGeneratedKeys();

            if (generatedKeys.next()) {
                exchangeRate.setId(generatedKeys.getInt(1));
            }

            return exchangeRate;
        }
    }

    public List<ExchangeRate> getAll() throws SQLException{
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(GET_ALL_SQL)) {

            List<ExchangeRate> exchangeRates = new LinkedList<>();
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                exchangeRates.add(
                        createExchangeRate(resultSet)
                );
            }

            return exchangeRates;
        }
    }

    public Optional<ExchangeRate> getByCodes(String baseCurrencyCode, String targetCurrencyCode) throws SQLException {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(GET_BY_CODE_SQL)) {

            statement.setString(1, baseCurrencyCode);
            statement.setString(2, targetCurrencyCode);

            ResultSet resultSet = statement.executeQuery();
            ExchangeRate exchangeRate = null;

            if (resultSet.next()) {
                exchangeRate = createExchangeRate(resultSet);
            }

            return Optional.ofNullable(exchangeRate);
        }
    }

    private ExchangeRate createExchangeRate(ResultSet resultSet) throws SQLException {
        Currency baseCurrency = new Currency(
                resultSet.getInt("base_currency_id"),
                resultSet.getString("base_currency_code"),
                resultSet.getString("base_currency_full_name"),
                resultSet.getString("base_currency_sign")
        );
        Currency targetCurrency = new Currency(
                resultSet.getInt("target_currency_id"),
                resultSet.getString("target_currency_code"),
                resultSet.getString("target_currency_full_name"),
                resultSet.getString("target_currency_sign")
        );

        return new ExchangeRate(
                resultSet.getInt("id"),
                baseCurrency,
                targetCurrency,
                resultSet.getBigDecimal("rate")
        );
    }

    public boolean update(ExchangeRate exchangeRate) throws SQLException {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {

            statement.setBigDecimal(1, exchangeRate.getRate());
            statement.setInt(2, exchangeRate.getBaseCurrency().getId());
            statement.setInt(3, exchangeRate.getTargetCurrency().getId());

            return statement.executeUpdate() > 0;
        }
    }

    public static ExchangeRateDAO getInstance() {
        return INSTANCE;
    }

    private ExchangeRateDAO() {
    }
}
