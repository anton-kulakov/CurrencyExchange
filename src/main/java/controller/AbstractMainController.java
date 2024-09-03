package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dto.RestErrorException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

public abstract class AbstractMainController extends HttpServlet {
    protected ObjectMapper objectMapper;
    public void init() throws ServletException {
        super.init();
        objectMapper = new ObjectMapper();
    }
    // этот doGet() Сергей написал для примера. Остальные методы можно писать в соответствии с ним
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            handleGet(req, resp);
        } catch (RestErrorException e) {
            sendError(e.code, e.message, resp);
        } catch (SQLException e) {
            sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error", resp);
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
}
