package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dao.CurrencyDAO;
import dto.CurrencyDTO;
import dto.Error;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

import static jakarta.servlet.http.HttpServletResponse.*;

public class CurrencyController extends HttpServlet {
    private final CurrencyDAO currencyDAO = CurrencyDAO.getInstance();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String currencyCode = req.getPathInfo().replaceAll("/", "");
        CurrencyDTO currencyReqDTO = new CurrencyDTO();
        currencyReqDTO.setCode(currencyCode);

        if (currencyCode.isEmpty() || currencyCode.isBlank()) {
            resp.setStatus(SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new Error(
                    SC_BAD_REQUEST,
                    "The currency code is empty."
            ));

            return;
        }

        try {
            Optional<CurrencyDTO> currencyDTO = currencyDAO.getByCode(currencyReqDTO);

            if (currencyDTO.isEmpty()) {
                resp.setStatus(SC_NOT_FOUND);
                objectMapper.writeValue(resp.getWriter(), new Error(
                        SC_NOT_FOUND,
                        "The requested currency was not found."
                ));

                return;
            }

            objectMapper.writeValue(resp.getWriter(), currencyDTO.get());
        } catch (SQLException e) {
            objectMapper.writeValue(resp.getWriter(), new Error(
                    SC_INTERNAL_SERVER_ERROR,
                    "The database is unavailable. Please try again later."
            ));
        }
    }
}
