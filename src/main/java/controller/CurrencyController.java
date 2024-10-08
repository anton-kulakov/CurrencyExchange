package controller;

import dto.CurrencyDTO;
import exception.InvalidParamException;
import exception.RestErrorException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Optional;

import static jakarta.servlet.http.HttpServletResponse.SC_NOT_FOUND;

public class CurrencyController extends AbstractMainController {
    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String currencyCode = req.getPathInfo().replaceAll("/", "");
        CurrencyDTO currencyReqDTO = new CurrencyDTO();
        currencyReqDTO.setCode(currencyCode);

        if (currencyReqDTO.getCode().isBlank() || !isCurrencyCodeFollowStandard(currencyReqDTO.getCode())) {
            throw new InvalidParamException();
        }

        Optional<CurrencyDTO> optionalCurrencyDTO = currencyDAO.getByCode(currencyReqDTO);

        if (optionalCurrencyDTO.isEmpty()) {
            throw new RestErrorException(
                    SC_NOT_FOUND,
                    "The requested currency was not found."
            );
        }

        objectMapper.writeValue(resp.getWriter(), optionalCurrencyDTO.get());
    }
}
