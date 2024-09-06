package exception;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class InvalidParamException extends Exception {
    public final int code;
    public final String message;
}
