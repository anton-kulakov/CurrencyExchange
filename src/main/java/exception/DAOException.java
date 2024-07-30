package exception;

import java.sql.SQLException;

public class DAOException extends RuntimeException {
    public DAOException(Throwable e) {
        super(e);
    }
}
