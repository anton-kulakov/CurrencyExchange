package exception;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DBException extends Exception {
    public final int code;
    public final String message;
}
