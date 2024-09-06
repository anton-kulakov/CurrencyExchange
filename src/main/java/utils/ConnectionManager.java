package utils;

import com.zaxxer.hikari.HikariDataSource;
import exception.DBException;
import lombok.Setter;

import java.sql.Connection;
import java.sql.SQLException;

public final class ConnectionManager {
    @Setter
    private static HikariDataSource dataSource;

    public static Connection getConnection() throws DBException {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new DBException();
        }
    }

    private ConnectionManager() {

    }
}
