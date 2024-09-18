package exception;

import static jakarta.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

public class InvalidRequestException extends Exception {
    public final int code = SC_BAD_REQUEST;
    public final String message = "The request is not valid";
}
