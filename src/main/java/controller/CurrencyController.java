package controller;

import dao.CurrencyDAO;
import dto.CurrencyDTO;
import dto.RestErrorException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Optional;

import static jakarta.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static jakarta.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static utils.CurrencyCodesValidator.isCurrencyCodeValid;

public class CurrencyController extends AbstractMainController {
    private final CurrencyDAO currencyDAO = CurrencyDAO.getInstance();

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String currencyCode = req.getPathInfo().replaceAll("/", "");
        CurrencyDTO currencyReqDTO = new CurrencyDTO();
        currencyReqDTO.setCode(currencyCode);

        if (!isCurrencyCodeValid2(currencyReqDTO)) {
            throw new RestErrorException(
                    SC_BAD_REQUEST,
                    "Currency code is invalid"
            );
        }

        Optional<CurrencyDTO> currencyDTO = currencyDAO.getByCode(currencyReqDTO);

        if (currencyDTO.isEmpty()) {
            throw new RestErrorException(
                    SC_NOT_FOUND,
                    "The requested currency was not found."
            );
        }

        objectMapper.writeValue(resp.getWriter(), currencyDTO.get());
    }

    private boolean isCurrencyCodeValid2(CurrencyDTO currencyReqDTO) {
        return !currencyReqDTO.getCode().isEmpty() ||
               !currencyReqDTO.getCode().isBlank() ||
               !isCurrencyCodeValid(currencyReqDTO.getCode());
    }
}
