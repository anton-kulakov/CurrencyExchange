package exception;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class InvalidRequestException extends Exception {
    public final int code;
    public final String message;
}
