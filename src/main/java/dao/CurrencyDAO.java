package dao;

import exception.DAOException;
import model.Currency;
import utils.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CurrencyDAO {
    private final static CurrencyDAO INSTANCE = new CurrencyDAO();
    private final static String SAVE_SQL = """
            INSERT INTO Currencies
            (Code, FullName, Sign) 
            VALUES (?, ?, ?)
             """;
    private final static String GET_ALL_SQL = """
            SELECT id, code, fullname, sign 
            FROM Currencies
             """;
    private final static String GET_BY_CODE_SQL = GET_ALL_SQL + """
            WHERE Code = ?
             """;

    public Currency save(Currency currency) {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SAVE_SQL, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, currency.getCode());
            statement.setString(2, currency.getFullName());
            statement.setString(3, currency.getSign());

            statement.executeUpdate();
            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                currency.setId(generatedKeys.getInt(1));
            }

            return currency;
        } catch (SQLException e) {
            throw new DAOException(e);
        }
    }

    public List<Currency> getAll() throws SQLException {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(GET_ALL_SQL)) {
            List<Currency> currencies = new ArrayList<>();
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                currencies.add(
                        createCurrency(resultSet)
                );
            }

            return currencies;
        }
    }

    public Optional<Currency> getByCode(String code) throws SQLException {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(GET_BY_CODE_SQL)) {

            statement.setString(1, code);

            ResultSet resultSet = statement.executeQuery();
            Currency currency = null;

            if (resultSet.next()) {
                currency = createCurrency(resultSet);
            }

            return Optional.ofNullable(currency);
        }
    }

    private static Currency createCurrency(ResultSet resultSet) throws SQLException {
        return new Currency(
                resultSet.getInt("id"),
                resultSet.getString("code"),
                resultSet.getString("fullname"),
                resultSet.getString("sign")
        );
    }

    public static CurrencyDAO getInstance() {
        return INSTANCE;
    }

    private CurrencyDAO() {
    }
}
