package dao;

import exception.DAOException;
import model.Currency;
import model.ExchangeRate;
import utils.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExchangeRateDAO {
    private final static ExchangeRateDAO INSTANCE = new ExchangeRateDAO();
    private final static String SAVE_SQL = """
            INSERT INTO ExchangeRates
            (BaseCurrencyID, TargetCurrencyID, Rate)
            VALUES (?, ?, ?)
            """;
    private final static String GET_ALL_SQL = """
            SELECT
            er.id,
            bc.id AS BaseCurrencyID,
            bc.code AS BaseCurrencyCode,
            bc.fullname AS BaseCurrencyFullName,
            bc.sign AS BaseCurrencySign,
            tc.id AS TargetCurrencyID,
            tc.code AS TargetCurrencyCode,
            tc.fullname AS TargetCurrencyFullName,
            tc.sign AS TargetCurrencySign,
            er.rate
            FROM ExchangeRates er
            INNER JOIN Currencies bc ON er.BaseCurrencyId = bc.id
            INNER JOIN Currencies tc ON er.TargetCurrencyId = tc.id
            """;
    private final static String UPDATE_SQL = """
            UPDATE ExchangeRates
            SET Rate = ? 
            WHERE BaseCurrencyId = ? AND TargetCurrencyId = ?
            """;
    private final static String GET_BY_CODE_SQL = GET_ALL_SQL + """
            WHERE bc.Code = ? AND tc.Code = ?
            """;

    public ExchangeRate save(ExchangeRate exchangeRate) {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SAVE_SQL, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, exchangeRate.getBaseCurrency().getId());
            statement.setInt(2, exchangeRate.getTargetCurrency().getId());
            statement.setBigDecimal(3, exchangeRate.getRate());

            statement.executeUpdate();
            ResultSet generatedKeys = statement.getGeneratedKeys();

            if (generatedKeys.next()) {
                exchangeRate.setId(generatedKeys.getInt(1));
            }

            return exchangeRate;
        } catch (SQLException e) {
            throw new DAOException(e);
        }
    }

    public List<ExchangeRate> getAll() {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(GET_ALL_SQL)) {
            List<ExchangeRate> exchangeRates = new ArrayList<>();
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                exchangeRates.add(
                        createExchangeRate(resultSet)
                );
            }

            return exchangeRates;
        } catch (SQLException e) {
            throw new DAOException(e);
        }
    }

    public ExchangeRate getByCode(ExchangeRate exchangeRate) {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(GET_BY_CODE_SQL)) {

            statement.setString(1, exchangeRate.getBaseCurrency().getCode());
            statement.setString(2, exchangeRate.getTargetCurrency().getCode());

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                exchangeRate.setId(resultSet.getInt("id"));
                exchangeRate.setRate(resultSet.getBigDecimal("rate"));
            }

            return exchangeRate;
        } catch (SQLException e) {
            throw new DAOException(e);
        }
    }

    private ExchangeRate createExchangeRate(ResultSet resultSet) throws SQLException {
        Currency baseCurrency = new Currency(
                resultSet.getInt("BaseCurrencyID"),
                resultSet.getString("BaseCurrencyCode"),
                resultSet.getString("BaseCurrencyFullName"),
                resultSet.getString("BaseCurrencySign")
        );
        Currency targetCurrency = new Currency(
                resultSet.getInt("TargetCurrencyID"),
                resultSet.getString("TargetCurrencyCode"),
                resultSet.getString("TargetCurrencyFullName"),
                resultSet.getString("TargetCurrencySign")
        );

        return new ExchangeRate(
                resultSet.getInt("id"),
                baseCurrency,
                targetCurrency,
                resultSet.getBigDecimal("rate")
        );
    }

    public boolean update(ExchangeRate exchangeRate) {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {

            statement.setBigDecimal(1, exchangeRate.getRate());
            statement.setInt(2, exchangeRate.getBaseCurrency().getId());
            statement.setInt(3, exchangeRate.getTargetCurrency().getId());

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DAOException(e);
        }
    }

    public static ExchangeRateDAO getInstance() {
        return INSTANCE;
    }

    private ExchangeRateDAO() {
    }
}
