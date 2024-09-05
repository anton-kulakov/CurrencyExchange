package exception;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RestErrorException extends Exception {
    public final int code;
    public final String message;
}
