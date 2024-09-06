package dao;

import dto.CurrencyDTO;
import entity.Currency;
import exception.DBException;
import org.modelmapper.ModelMapper;
import utils.ConnectionManager;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static jakarta.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

public class CurrencyDAO {
    private final static CurrencyDAO INSTANCE = new CurrencyDAO();
    private final ModelMapper modelMapper = new ModelMapper();
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

    public Optional<CurrencyDTO> save(CurrencyDTO currencyDTO) throws DBException {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SAVE_SQL, Statement.RETURN_GENERATED_KEYS)) {

            String code = currencyDTO.getCode();
            String name = currencyDTO.getName();
            String sign = currencyDTO.getSign();

            statement.setString(1, code);
            statement.setString(2, name);
            statement.setString(3, sign);

            statement.executeUpdate();
            ResultSet generatedKeys = statement.getGeneratedKeys();

            Currency currency = null;
            CurrencyDTO currencyRespDTO = null;

            if (generatedKeys.next()) {
                currency = new Currency(
                        generatedKeys.getInt(1),
                        code,
                        name,
                        sign
                );
            }

            if (currency != null) {
                currencyRespDTO = modelMapper.map(currency, CurrencyDTO.class);
            }

            return Optional.ofNullable(currencyRespDTO);
        } catch (SQLException e) {
            throw new DBException();
        }
    }

    public List<CurrencyDTO> getAll() throws DBException {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(GET_ALL_SQL)) {

            List<Currency> currencies = new LinkedList<>();
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                currencies.add(
                        createCurrency(resultSet)
                );
            }

            return currencies.stream()
                    .map(currency -> modelMapper.map(currency, CurrencyDTO.class))
                    .collect(Collectors.toList());
        } catch (SQLException e) {
            throw new DBException();
        }
    }

    public Optional<CurrencyDTO> getByCode(CurrencyDTO currencyDTO) throws DBException {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(GET_BY_CODE_SQL)) {

            statement.setString(1, currencyDTO.getCode());

            ResultSet resultSet = statement.executeQuery();
            Currency currency = null;
            CurrencyDTO currencyRespDTO = null;

            if (resultSet.next()) {
                currency = createCurrency(resultSet);
            }

            if (currency != null) {
                currencyRespDTO = modelMapper.map(currency, CurrencyDTO.class);
            }

            return Optional.ofNullable(currencyRespDTO);
        } catch (SQLException e) {
            throw new DBException();
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
             throw new DBException();
         }
    }

    public static CurrencyDAO getInstance() {
        return INSTANCE;
    }

    private CurrencyDAO() {
    }
}
