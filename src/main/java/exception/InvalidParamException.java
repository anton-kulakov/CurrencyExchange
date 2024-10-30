package exception;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class InvalidParamException extends RuntimeException {
    public final int code;
    public final String message;
}
