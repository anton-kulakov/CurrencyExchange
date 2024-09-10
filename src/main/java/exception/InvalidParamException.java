package exception;

import lombok.AllArgsConstructor;

import static jakarta.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

@AllArgsConstructor
public class InvalidParamException extends Exception {
    public final int code = SC_BAD_REQUEST;
    public final String message = "One or more parameters are not valid";
}
