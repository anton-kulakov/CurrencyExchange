package dao;

import dto.CurrencyDTO;
import dto.ExchangeRateReqDTO;
import dto.ExchangeRateRespDTO;
import entity.Currency;
import entity.ExchangeRate;
import exception.DBException;
import org.modelmapper.ModelMapper;
import utils.ConnectionManager;

import java.math.BigDecimal;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ExchangeRateDAO {
    private final static ExchangeRateDAO INSTANCE = new ExchangeRateDAO();
    private final CurrencyDAO currencyDAO = CurrencyDAO.getInstance();
    private final ModelMapper modelMapper = new ModelMapper();
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

    public Optional<ExchangeRateRespDTO> save(ExchangeRateReqDTO exRateReqDTO) throws DBException {
        Connection connection = ConnectionManager.getConnection();
        try (PreparedStatement statement = connection.prepareStatement(SAVE_SQL, Statement.RETURN_GENERATED_KEYS)) {

            CurrencyDTO baseCurrencyDTO = new CurrencyDTO();
            CurrencyDTO targetCurrencyDTO = new CurrencyDTO();
            baseCurrencyDTO.setCode(exRateReqDTO.getBaseCurrencyCode());
            targetCurrencyDTO.setCode(exRateReqDTO.getTargetCurrencyCode());

            connection.setAutoCommit(false);
            Optional<CurrencyDTO> optBaseCurrencyDTO = currencyDAO.getByCode(baseCurrencyDTO);
            Optional<CurrencyDTO> optTargetCurrencyDTO = currencyDAO.getByCode(targetCurrencyDTO);

            if (optBaseCurrencyDTO.isEmpty() || optTargetCurrencyDTO.isEmpty()) {
                throw new DBException();
            }

            Currency baseCurrency = modelMapper.map(optBaseCurrencyDTO.get(), Currency.class);
            Currency targetCurrency = modelMapper.map(optTargetCurrencyDTO.get(), Currency.class);
            BigDecimal rate = exRateReqDTO.getRate();

            statement.setInt(1, baseCurrency.getId());
            statement.setInt(2, targetCurrency.getId());
            statement.setBigDecimal(3, rate);

            statement.executeUpdate();
            ResultSet generatedKeys = statement.getGeneratedKeys();

            ExchangeRate exchangeRate = null;
            ExchangeRateRespDTO exRateRespDTO = null;

            if (generatedKeys.next()) {
                exchangeRate = new ExchangeRate(
                        generatedKeys.getInt(1),
                        baseCurrency,
                        targetCurrency,
                        rate
                );
            }

            if (exchangeRate != null) {
                exRateRespDTO = modelMapper.map(exchangeRate, ExchangeRateRespDTO.class);
            }
            connection.commit();

            return Optional.ofNullable(exRateRespDTO);
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }

            throw new DBException();
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public List<ExchangeRateRespDTO> getAll() throws DBException {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(GET_ALL_SQL)) {

            List<ExchangeRate> exchangeRates = new LinkedList<>();
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                exchangeRates.add(
                        createExchangeRate(resultSet)
                );
            }

            return exchangeRates.stream()
                    .map(exchangeRate -> modelMapper.map(exchangeRate, ExchangeRateRespDTO.class))
                    .collect(Collectors.toList());
        } catch (SQLException e) {
            throw new DBException();
        }
    }

    public Optional<ExchangeRateRespDTO> getByCodes(ExchangeRateReqDTO exRateReqDTO) throws DBException {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(GET_BY_CODES_SQL)) {

            statement.setString(1, exRateReqDTO.getBaseCurrencyCode());
            statement.setString(2, exRateReqDTO.getTargetCurrencyCode());

            ResultSet resultSet = statement.executeQuery();

            ExchangeRate exchangeRate = null;
            ExchangeRateRespDTO exRateRespDTO = null;

            if (resultSet.next()) {
                exchangeRate = createExchangeRate(resultSet);
            }

            if (exchangeRate != null) {
                exRateRespDTO = modelMapper.map(exchangeRate, ExchangeRateRespDTO.class);
            }

            return Optional.ofNullable(exRateRespDTO);
        } catch (SQLException e) {
            throw new DBException();
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
            throw new DBException();
        }
    }

    public boolean update(ExchangeRateRespDTO exRateRespDTO) throws DBException {
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {

            statement.setBigDecimal(1, exRateRespDTO.getRate());
            statement.setInt(2, exRateRespDTO.getBaseCurrency().getId());
            statement.setInt(3, exRateRespDTO.getTargetCurrency().getId());

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DBException();
        }
    }

    public static ExchangeRateDAO getInstance() {
        return INSTANCE;
    }

    private ExchangeRateDAO() {
    }
}
