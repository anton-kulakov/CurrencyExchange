package controller;

import dto.CurrencyRequestDTO;
import entity.Currency;
import exception.InvalidParamException;
import exception.InvalidRequestException;
import exception.RestErrorException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.modelmapper.ModelMapper;

import java.util.Map;
import java.util.Set;

import static jakarta.servlet.http.HttpServletResponse.*;

public class CurrenciesController extends AbstractMainController {
    private final static ModelMapper modelMapper = new ModelMapper();

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        objectMapper.writeValue(resp.getWriter(), currencyDAO.getAll());
    }

    @Override
    protected void handlePost(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        if (!isRequestValid(req.getParameterMap())) {
            throw new InvalidRequestException();
        }

        String code = req.getParameter("code");
        String name = req.getParameter("name");
        String sign = req.getParameter("sign");

        CurrencyRequestDTO currencyRequestDTO = new CurrencyRequestDTO(code, name, sign);

        if (!isCurrencyCodeFollowStandard(currencyRequestDTO.getCode())) {
            throw new RestErrorException(SC_BAD_REQUEST, "The currency code must follow the ISO 4217 standard");
        }

        if (!isParametersValid(currencyRequestDTO)) {
            throw new InvalidParamException();
        }

        if (currencyDAO.getByCode(currencyRequestDTO.getCode()).isPresent()) {
            throw new RestErrorException(SC_CONFLICT, "A currency with this code already exists");
        }

        Currency currency = currencyDAO.save(convertToEntity(currencyRequestDTO))
                .orElseThrow(() -> new RestErrorException(SC_INTERNAL_SERVER_ERROR, "Something happened with the database. Please try again later!"));

        resp.setStatus(SC_CREATED);
        objectMapper.writeValue(resp.getWriter(), currency);
    }

    private static Currency convertToEntity(CurrencyRequestDTO currencyRequestDTO) {
        return modelMapper.map(currencyRequestDTO, Currency.class);
    }

    private boolean isRequestValid(Map<String, String[]> parameterMap) {
        Set<String> requiredParams = Set.of("code", "name", "sign");

        return parameterMap.keySet().containsAll(requiredParams);
    }

    private boolean isParametersValid(CurrencyRequestDTO currencyRequestDTO) {
        return !currencyRequestDTO.getCode().isBlank() &&
               !currencyRequestDTO.getName().isBlank() &&
               !currencyRequestDTO.getSign().isBlank();
    }
}
