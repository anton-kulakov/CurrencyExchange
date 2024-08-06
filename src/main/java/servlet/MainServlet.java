package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;

public abstract class MainServlet extends HttpServlet {
    public void init() throws ServletException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new ServletException("Failed to load SQLite JDBC driver", e);
        }
    }
}
