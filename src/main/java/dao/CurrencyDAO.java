package dao;

import entity.Currency;
import exception.DBException;
import utils.ConnectionManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static jakarta.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

public class CurrencyDAO {
    private final static CurrencyDAO INSTANCE = new CurrencyDAO();
    private final static String SAVE_SQL = """
            INSERT INTO currencies
            (code, full_name, sign)
            VALUES (?, ?, ?)
             """;
    private final static String GET_ALL_SQL = """
            SELECT id, code, full_name, sign
            FROM currencies
             """;
    private final static String GET_BY_CODE_SQL = GET_ALL_SQL + """
            WHERE code = ?
             """;

    public Optional<Currency> save(Currency currency) throws DBException {
        String currencyCode = currency.getCode();

        try (var connection = ConnectionManager.getConnection();
             var statement = connection.prepareStatement(SAVE_SQL, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, currency.getCode());
            statement.setString(2, currency.getName());
            statement.setString(3, currency.getSign());
            statement.executeUpdate();

            ResultSet generatedKeys = statement.getGeneratedKeys();

            if (generatedKeys.next()) {
                currency.setId(generatedKeys.getInt(1));
            } else {
                currency = null;
            }

            return Optional.ofNullable(currency);
        } catch (SQLException e) {
            throw new DBException(SC_INTERNAL_SERVER_ERROR,
                    String.format("The currency %s could not be saved. Something happened with the database. Please try again later!", currencyCode));
        }
    }

    public List<Currency> getAll() throws DBException {
        try (var connection = ConnectionManager.getConnection();
             var statement = connection.prepareStatement(GET_ALL_SQL)) {

            List<Currency> currencies = new LinkedList<>();
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                currencies.add(createCurrency(resultSet));
            }

            return currencies;
        } catch (SQLException e) {
            throw new DBException(SC_INTERNAL_SERVER_ERROR, "Unable to retrieve a list of currencies");
        }
    }

    public Optional<Currency> getByCode(String code) throws DBException {
        try (var connection = ConnectionManager.getConnection();
             var statement = connection.prepareStatement(GET_BY_CODE_SQL)) {

            statement.setString(1, code);

            ResultSet resultSet = statement.executeQuery();

            Currency currency = null;

            if (resultSet.next()) {
                currency = createCurrency(resultSet);
            }

            return Optional.ofNullable(currency);
        } catch (SQLException e) {
            throw new DBException(SC_INTERNAL_SERVER_ERROR,
                    String.format("The attempt to retrieve information about currency %s was unsuccessful", code));
        }
    }

    private static Currency createCurrency(ResultSet resultSet) throws DBException {
        try {
            return new Currency(
                    resultSet.getInt("id"),
                    resultSet.getString("code"),
                    resultSet.getString("full_name"),
                    resultSet.getString("sign")
            );
        } catch (SQLException e) {
            throw new DBException(SC_INTERNAL_SERVER_ERROR, "Something happened with the database. Please try again later!");
        }
    }

    public static CurrencyDAO getInstance() {
        return INSTANCE;
    }

    private CurrencyDAO() {
    }
}
