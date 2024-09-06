package exception;

import static jakarta.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

public class DBException extends Exception {
    public final int code = SC_INTERNAL_SERVER_ERROR;
    public final String message = "Something happened with the database. Please try again later!";
}
