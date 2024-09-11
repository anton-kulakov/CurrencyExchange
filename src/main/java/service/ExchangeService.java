package service;

import dao.ExchangeRateDAO;
import dto.*;
import entity.Currency;
import exception.DBException;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

public class ExchangeService {
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

    public Optional<ExchangeRespDTO> makeExchange(ExchangeReqDTO exchangeReqDTO) throws DBException {
        Optional<ExchangeRateRespDTO> optionalExchangeRate = getOptionalExchangeRate(exchangeReqDTO);
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

    private Optional<ExchangeRateRespDTO> getOptionalExchangeRate(ExchangeReqDTO exchangeReqDTO) throws DBException {
        Optional<ExchangeRateRespDTO> optionalExchangeRate = getDirectExchangeRate(exchangeReqDTO);

        if (optionalExchangeRate.isEmpty()) {
            optionalExchangeRate = getFromReversedExchangeRate(exchangeReqDTO);
        }

        if (optionalExchangeRate.isEmpty()) {
            optionalExchangeRate = getFromCrossExchangeRate(exchangeReqDTO);
        }

        return optionalExchangeRate;
    }

    private Optional<ExchangeRateRespDTO> getDirectExchangeRate(ExchangeReqDTO exchangeReqDTO) throws DBException {
        ExchangeRateReqDTO directExchangeRate = modelMapper.map(exchangeReqDTO, ExchangeRateReqDTO.class);

        return exchangeRateDAO.getByCodes(directExchangeRate);
    }

    private Optional<ExchangeRateRespDTO> getFromReversedExchangeRate(ExchangeReqDTO exchangeReqDTO) throws DBException {
        String from = exchangeReqDTO.getFrom();
        String to = exchangeReqDTO.getTo();

        exchangeReqDTO.setFrom(to);
        exchangeReqDTO.setTo(from);

        ExchangeRateReqDTO exRateReqDTO = modelMapper.map(exchangeReqDTO, ExchangeRateReqDTO.class);

        Optional<ExchangeRateRespDTO> reversedExchangeRate = exchangeRateDAO.getByCodes(exRateReqDTO);
        Optional<ExchangeRateRespDTO> directExchangeRate = Optional.empty();

        if (reversedExchangeRate.isPresent()) {

            directExchangeRate = exchangeRateDAO.save(new ExchangeRateReqDTO(
                    from,
                    to,
                    BigDecimal.ONE.divide(reversedExchangeRate.get().getRate(), 6, RoundingMode.HALF_UP))
            );
        }

        return directExchangeRate;
    }

    private Optional<ExchangeRateRespDTO> getFromCrossExchangeRate(ExchangeReqDTO exchangeReqDTO) throws DBException {
        ExchangeRateReqDTO exRateReqDTOFrom = new ExchangeRateReqDTO();
        ExchangeRateReqDTO exRateReqDTOTo = new ExchangeRateReqDTO();

        exRateReqDTOFrom.setBaseCurrencyCode("USD");
        exRateReqDTOFrom.setTargetCurrencyCode(exchangeReqDTO.getFrom());

        exRateReqDTOTo.setBaseCurrencyCode("USD");
        exRateReqDTOTo.setTargetCurrencyCode(exchangeReqDTO.getTo());

        Optional<ExchangeRateRespDTO> optionalExchangeRateUSDFrom = exchangeRateDAO.getByCodes(exRateReqDTOFrom);
        Optional<ExchangeRateRespDTO> optionalExchangeRateUSDTo = exchangeRateDAO.getByCodes(exRateReqDTOTo);

        ExchangeRateRespDTO exchangeRateRespDTO = null;

        if (optionalExchangeRateUSDFrom.isPresent() && optionalExchangeRateUSDTo.isPresent()) {
            BigDecimal rate = optionalExchangeRateUSDFrom.get().getRate().divide(
                    optionalExchangeRateUSDTo.get().getRate(),
                    6,
                    RoundingMode.HALF_UP
            );

            CurrencyDTO from = new CurrencyDTO();
            CurrencyDTO to = new CurrencyDTO();
            from.setCode(exchangeReqDTO.getFrom());
            to.setCode(exchangeReqDTO.getTo());

            exchangeRateRespDTO = new ExchangeRateRespDTO();
            exchangeRateRespDTO.setBaseCurrency(modelMapper.map(from, Currency.class));
            exchangeRateRespDTO.setTargetCurrency(modelMapper.map(to, Currency.class));
            exchangeRateRespDTO.setRate(rate);
        }

        return Optional.ofNullable(exchangeRateRespDTO);
    }
}
