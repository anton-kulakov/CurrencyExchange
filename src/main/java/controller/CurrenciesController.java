package controller;

import dto.CurrencyDTO;
import exception.InvalidParamException;
import exception.InvalidRequestException;
import exception.RestErrorException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static jakarta.servlet.http.HttpServletResponse.*;

public class CurrenciesController extends AbstractMainController {
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

        CurrencyDTO currencyReqDTO = new CurrencyDTO(code, name, sign);

        if (!isCurrencyCodeFollowStandard(currencyReqDTO.getCode())) {
            throw new RestErrorException(
                    SC_BAD_REQUEST,
                    "The currency code must follow the ISO 4217 standard"
                    );
        }

        if (!isParametersValid(currencyReqDTO)) {
            throw new InvalidParamException();
        }

        if (currencyDAO.getByCode(currencyReqDTO).isPresent()) {
            throw new RestErrorException(
                    SC_CONFLICT,
                    "A currency with this code already exists"
            );
        }

        Optional<CurrencyDTO> optionalCurrencyDTO = currencyDAO.save(currencyReqDTO);

        if (optionalCurrencyDTO.isEmpty()) {
            throw new RestErrorException(
                    SC_INTERNAL_SERVER_ERROR,
                    "Something happened with the database. Please try again later!"
            );
        }

        resp.setStatus(SC_CREATED);
        objectMapper.writeValue(resp.getWriter(), optionalCurrencyDTO.get());
    }

    private boolean isRequestValid(Map<String, String[]> parameterMap) {
        Set<String> requiredParams = Set.of("code", "name", "sign");

        return parameterMap.keySet().containsAll(requiredParams);
    }

    private boolean isParametersValid(CurrencyDTO currencyReqDTO) {
        return !currencyReqDTO.getCode().isBlank() &&
               !currencyReqDTO.getName().isBlank() &&
               !currencyReqDTO.getSign().isBlank();
    }
}
