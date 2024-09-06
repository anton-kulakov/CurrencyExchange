package controller;

import dto.CurrencyDTO;
import exception.InvalidParamException;
import exception.RestErrorException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Optional;

import static jakarta.servlet.http.HttpServletResponse.*;
import static utils.CurrencyCodesValidator.isCurrencyCodeValid;

public class CurrenciesController extends AbstractMainController {
    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        objectMapper.writeValue(resp.getWriter(), currencyDAO.getAll());
    }

    @Override
    protected void handlePost(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String code = req.getParameter("code");
        String name = req.getParameter("name");
        String sign = req.getParameter("sign");

        CurrencyDTO currencyReqDTO = new CurrencyDTO(code, name, sign);

        if (!isParametersValid(currencyReqDTO)) {
            throw new InvalidParamException(
                    SC_BAD_REQUEST,
                    "One or more parameters are not valid"
            );
        }

        if (currencyDAO.getByCode(currencyReqDTO).isPresent()) {
            throw new RestErrorException(
                    SC_CONFLICT,
                    "A currency with this code already exists"
            );
        }

        Optional<CurrencyDTO> currencyDTO = currencyDAO.save(currencyReqDTO);

        if (currencyDTO.isEmpty()) {
            throw new RestErrorException(
                    SC_INTERNAL_SERVER_ERROR,
                    "Something happened with the database. Please try again later!"
            );
        }

        resp.setStatus(SC_CREATED);
        objectMapper.writeValue(resp.getWriter(), currencyDTO.get());
    }

    private boolean isParametersValid(CurrencyDTO currencyReqDTO) {
        return !currencyReqDTO.getCode().isBlank() &&
               !currencyReqDTO.getName().isBlank() &&
               !currencyReqDTO.getSign().isBlank() &&
               isCurrencyCodeValid(currencyReqDTO.getCode());
    }
}
