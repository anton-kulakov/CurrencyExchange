package dao;

import entity.Currency;
import entity.ExchangeRate;
import exception.DBException;
import utils.ConnectionManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static jakarta.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

public class ExchangeRateDAO {
    private final static ExchangeRateDAO INSTANCE = new ExchangeRateDAO();
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
    private final static String GET_BY_CODES_SQL = GET_ALL_SQL + """
            WHERE bc.code = ? AND tc.code = ?
            """;

    public Optional<ExchangeRate> save(ExchangeRate exchangeRate) throws DBException {
        try (var connection = ConnectionManager.getConnection();
             var statement = connection.prepareStatement(SAVE_SQL, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, exchangeRate.getBaseCurrency().getId());
            statement.setInt(2, exchangeRate.getTargetCurrency().getId());
            statement.setBigDecimal(3, exchangeRate.getRate());
            statement.executeUpdate();

            ResultSet generatedKeys = statement.getGeneratedKeys();

            if (generatedKeys.next()) {
                exchangeRate.setId(generatedKeys.getInt(1));
            } else {
                exchangeRate = null;
            }

            return Optional.ofNullable(exchangeRate);
        } catch (SQLException e) {
            throw new DBException(SC_INTERNAL_SERVER_ERROR, "Something happened with the database. Please try again later!");
        }
    }

    public List<ExchangeRate> getAll() throws DBException {
        try (var connection = ConnectionManager.getConnection();
             var statement = connection.prepareStatement(GET_ALL_SQL)) {

            List<ExchangeRate> exchangeRates = new LinkedList<>();
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                exchangeRates.add(createExchangeRate(resultSet));
            }

            return exchangeRates;
        } catch (SQLException e) {
            throw new DBException(SC_INTERNAL_SERVER_ERROR, "Unable to retrieve a list of exchange rates");
        }
    }

    public Optional<ExchangeRate> getByCodes(String baseCurrencyCode, String targetCurrencyCode) throws DBException {
        String currencyPair = baseCurrencyCode + targetCurrencyCode;

        try (var connection = ConnectionManager.getConnection();
             var statement = connection.prepareStatement(GET_BY_CODES_SQL)) {

            statement.setString(1, baseCurrencyCode);
            statement.setString(2, targetCurrencyCode);

            ResultSet resultSet = statement.executeQuery();

            ExchangeRate exchangeRate = null;

            if (resultSet.next()) {
                exchangeRate = createExchangeRate(resultSet);
            }

            return Optional.ofNullable(exchangeRate);
        } catch (SQLException e) {
            throw new DBException(SC_INTERNAL_SERVER_ERROR,
                    String.format("The attempt to retrieve information about exchange rate %s was unsuccessful", currencyPair));
        }
    }

    private ExchangeRate createExchangeRate(ResultSet resultSet) throws DBException {
        try {
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
        } catch (SQLException e) {
            throw new DBException(SC_INTERNAL_SERVER_ERROR, "Something happened with the database. Please try again later!");
        }
    }

    public boolean update(ExchangeRate exchangeRate) throws DBException {
        String currencyPair = exchangeRate.getBaseCurrency().getCode() + exchangeRate.getTargetCurrency().getCode();

        try (var connection = ConnectionManager.getConnection();
             var statement = connection.prepareStatement(UPDATE_SQL)) {

            statement.setBigDecimal(1, exchangeRate.getRate());
            statement.setInt(2, exchangeRate.getBaseCurrency().getId());
            statement.setInt(3, exchangeRate.getTargetCurrency().getId());

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DBException(SC_INTERNAL_SERVER_ERROR,
                    String.format("The exchange rate %s could not be updated. Something happened with the database. Please try again later!", currencyPair));
        }
    }

    public static ExchangeRateDAO getInstance() {
        return INSTANCE;
    }

    private ExchangeRateDAO() {
    }
}
