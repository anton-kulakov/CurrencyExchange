package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dao.CurrencyDAO;
import dao.ExchangeRateDAO;
import dto.ExchangeRateReqDTO;
import dto.ExchangeRateRespDTO;
import exception.DBException;
import exception.InvalidParamException;
import exception.RestErrorException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

public abstract class AbstractMainController extends HttpServlet {
    protected ObjectMapper objectMapper;
    protected CurrencyDAO currencyDAO;
    protected ExchangeRateDAO exchangeRateDAO;

    public void init() throws ServletException {
        super.init();
        objectMapper = new ObjectMapper();
        currencyDAO = CurrencyDAO.getInstance();
        exchangeRateDAO = ExchangeRateDAO.getInstance();
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String method = req.getMethod();

        if ("PATCH".equalsIgnoreCase(method)) {
            doPatch(req, resp);
        } else {
            super.service(req, resp);
        }
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            handleGet(req, resp);
        } catch (RestErrorException e) {
            sendError(e.code, e.message, resp);
        } catch (InvalidParamException e) {
            sendError(e.code, e.message, resp);
        } catch (Exception e) {
            sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Fatal error", resp);
        }
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try {
            handlePost(req, resp);
        } catch (RestErrorException e) {
            sendError(e.code, e.message, resp);
        } catch (InvalidParamException e) {
            sendError(e.code, e.message, resp);
        } catch (Exception e) {
            sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Fatal error", resp);
        }
    }

    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) {
        try {
            handlePatch(req, resp);
        } catch (RestErrorException e) {
            sendError(e.code, e.message, resp);
        } catch (InvalidParamException e) {
            sendError(e.code, e.message, resp);
        } catch (DBException e) {
            sendError(e.code, e.message, resp);
        } catch (Exception e) {
            sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Fatal error", resp);
        }
    }


    protected void sendError(int code, String message, HttpServletResponse resp) {
        try {
            resp.setStatus(code);
            resp.getWriter().println();
            resp.getWriter().println(objectMapper.createObjectNode().put("message", message));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    abstract protected void handleGet(HttpServletRequest req, HttpServletResponse resp) throws Exception;

    protected void handlePost(HttpServletRequest req, HttpServletResponse resp) throws Exception {

    }

    protected void handlePatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {

    }

    protected void updateReversedExchangeRate(ExchangeRateReqDTO exRateReqDTO) throws DBException {
        ExchangeRateReqDTO reversedExRateReqDTO = new ExchangeRateReqDTO(
                exRateReqDTO.getTargetCurrencyCode(),
                exRateReqDTO.getBaseCurrencyCode(),
                BigDecimal.ONE.divide(exRateReqDTO.getRate(), 6, RoundingMode.HALF_UP)
        );

        Optional<ExchangeRateRespDTO> optReversedExRateRespDTO = exchangeRateDAO.getByCodes(reversedExRateReqDTO);

        if (optReversedExRateRespDTO.isPresent()) {
            optReversedExRateRespDTO.get().setRate(reversedExRateReqDTO.getRate());

            exchangeRateDAO.update(optReversedExRateRespDTO.get());
        }
    }
}
