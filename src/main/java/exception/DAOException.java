package exception;

public class DAOException extends RuntimeException {
    public DAOException(Throwable e) {
        super(e);
    }
}
