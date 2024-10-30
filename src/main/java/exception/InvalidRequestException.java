package exception;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class InvalidRequestException extends RuntimeException {
    public final int code;
    public final String message;
}
