package service;

import dao.ExchangeRateDAO;
import dto.ExchangeRateReqDTO;
import dto.ExchangeReqDTO;
import dto.ExchangeRespDTO;
import entity.ExchangeRate;
import exception.DBException;
import exception.RestErrorException;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

public class ExchangeService {
    ExchangeRateService exchangeRateService = new ExchangeRateService();
    ExchangeRateDAO exchangeRateDAO = ExchangeRateDAO.getInstance();
    private final ModelMapper modelMapper;

    public ExchangeService() {

        this.modelMapper = new ModelMapper();
        PropertyMap<ExchangeReqDTO, ExchangeRateReqDTO> exchangeMap = new PropertyMap<ExchangeReqDTO, ExchangeRateReqDTO>() {
            @Override
            protected void configure() {
                map().setBaseCurrencyCode(source.getFrom());
                map().setTargetCurrencyCode(source.getTo());
                skip(destination.getRate());
            }
        };

        modelMapper.addMappings(exchangeMap);
    }

    public Optional<ExchangeRespDTO> makeExchange(ExchangeReqDTO exchangeReqDTO) throws DBException, RestErrorException {
        Optional<ExchangeRate> optionalExchangeRate = getOptionalExchangeRate(exchangeReqDTO);
        ExchangeRespDTO exchangeRespDTO = null;

        if (optionalExchangeRate.isPresent()) {
            BigDecimal rate = optionalExchangeRate.get().getRate();
            BigDecimal amount = exchangeReqDTO.getAmount();

            exchangeRespDTO = new ExchangeRespDTO(
                    optionalExchangeRate.get().getBaseCurrency(),
                    optionalExchangeRate.get().getTargetCurrency(),
                    rate,
                    amount,
                    amount.multiply(rate).setScale(2, RoundingMode.HALF_UP)
            );
        }

        return Optional.ofNullable(exchangeRespDTO);
    }

    private Optional<ExchangeRate> getOptionalExchangeRate(ExchangeReqDTO exchangeReqDTO) throws DBException, RestErrorException {
        Optional<ExchangeRate> optionalExchangeRate = getDirectExchangeRate(exchangeReqDTO);

        if (optionalExchangeRate.isEmpty()) {
            optionalExchangeRate = getFromReversedExchangeRate(exchangeReqDTO);
        }

        if (optionalExchangeRate.isEmpty()) {
            optionalExchangeRate = getFromCrossExchangeRate(exchangeReqDTO);
        }

        return optionalExchangeRate;
    }

    private Optional<ExchangeRate> getDirectExchangeRate(ExchangeReqDTO exchangeReqDTO) throws DBException {
        ExchangeRateReqDTO directExchangeRate = modelMapper.map(exchangeReqDTO, ExchangeRateReqDTO.class);

        return exchangeRateDAO.getByCodes(directExchangeRate.getBaseCurrencyCode(), directExchangeRate.getTargetCurrencyCode());
    }

    private Optional<ExchangeRate> getFromReversedExchangeRate(ExchangeReqDTO exchangeReqDTO) throws DBException, RestErrorException {
        Optional<ExchangeRate> reversedExchangeRate = exchangeRateDAO.getByCodes(exchangeReqDTO.getTo(), exchangeReqDTO.getFrom());
        Optional<ExchangeRate> directExchangeRate = Optional.empty();

        if (reversedExchangeRate.isPresent()) {
            directExchangeRate = exchangeRateService.preserve(new ExchangeRateReqDTO(
                    exchangeReqDTO.getFrom(),
                    exchangeReqDTO.getTo(),
                    BigDecimal.ONE.divide(reversedExchangeRate.get().getRate(), 6, RoundingMode.HALF_UP))
            );
        }

        return directExchangeRate;
    }

    private Optional<ExchangeRate> getFromCrossExchangeRate(ExchangeReqDTO exchangeReqDTO) throws DBException {
        String baseCurrencyCode = "USD";

        Optional<ExchangeRate> optExchangeRateUSDFrom = exchangeRateDAO.getByCodes(baseCurrencyCode, exchangeReqDTO.getFrom());
        Optional<ExchangeRate> optExchangeRateUSDTo = exchangeRateDAO.getByCodes(baseCurrencyCode, exchangeReqDTO.getTo());

        ExchangeRate exchangeRate = null;

        if (optExchangeRateUSDFrom.isPresent() && optExchangeRateUSDTo.isPresent()) {
            BigDecimal rate = optExchangeRateUSDTo.get().getRate().divide(
                    optExchangeRateUSDFrom.get().getRate(),
                    6,
                    RoundingMode.HALF_UP
            );

            exchangeRate = new ExchangeRate();
            exchangeRate.setBaseCurrency(optExchangeRateUSDFrom.get().getBaseCurrency());
            exchangeRate.setTargetCurrency(optExchangeRateUSDTo.get().getBaseCurrency());
            exchangeRate.setRate(rate);
        }

        return Optional.ofNullable(exchangeRate);
    }
}
