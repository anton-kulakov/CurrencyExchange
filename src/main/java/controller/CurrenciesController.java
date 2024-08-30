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
import java.util.List;
import java.util.Optional;

import static jakarta.servlet.http.HttpServletResponse.*;

public class CurrenciesController extends HttpServlet {
    private final CurrencyDAO currencyDAO = CurrencyDAO.getInstance();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            List<CurrencyDTO> currencies = currencyDAO.getAll();
            objectMapper.writeValue(resp.getWriter(), currencies);
        } catch (SQLException e) {
            resp.setStatus(SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(resp.getWriter(), new Error(
                    SC_INTERNAL_SERVER_ERROR,
                    "The database is unavailable. Please try again later."
            ));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String code = req.getParameter("code");
        String name = req.getParameter("name");
        String sign = req.getParameter("sign");

        CurrencyDTO currencyReqDTO = new CurrencyDTO(code, name, sign);

        if (code.isEmpty() || code.isBlank()) {
            resp.setStatus(SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new Error(
                    SC_BAD_REQUEST,
                    "The currency code is empty."
            ));

            return;
        }

        if (name.isEmpty() || name.isBlank()) {
            resp.setStatus(SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new Error(
                    SC_BAD_REQUEST,
                    "The name of currency is empty."
            ));

            return;
        }

        if (sign.isEmpty() || sign.isBlank()) {
            resp.setStatus(SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), new Error(
                    SC_BAD_REQUEST,
                    "The sign of currency is empty."
            ));

            return;
        }

        try {
            if (currencyDAO.getByCode(currencyReqDTO).isPresent()) {
                resp.setStatus(SC_CONFLICT);
                objectMapper.writeValue(resp.getWriter(), new Error(
                        SC_CONFLICT,
                        "A currency with this code already exists."
                ));

                return;
            }

            Optional<CurrencyDTO> currencyDTO = currencyDAO.save(currencyReqDTO);
            resp.setStatus(SC_CREATED);
            objectMapper.writeValue(resp.getWriter(), currencyDTO.get());
        } catch (SQLException e) {
            resp.setStatus(SC_INTERNAL_SERVER_ERROR);
            objectMapper.writeValue(resp.getWriter(), new Error(
                    SC_INTERNAL_SERVER_ERROR,
                    "The database is unavailable. Please try again later."
            ));
        }
    }
}
