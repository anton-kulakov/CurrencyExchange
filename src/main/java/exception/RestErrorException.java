package exception;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RestErrorException extends RuntimeException {
    public final int code;
    public final String message;
}
