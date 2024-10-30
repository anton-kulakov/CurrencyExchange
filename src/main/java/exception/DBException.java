package exception;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DBException extends RuntimeException {
    public final int code;
    public final String message;
}
