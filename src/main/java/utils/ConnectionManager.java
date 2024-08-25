package utils;

import com.zaxxer.hikari.HikariDataSource;
import lombok.Setter;

import java.sql.Connection;
import java.sql.SQLException;

public final class ConnectionManager {
    @Setter
    private static HikariDataSource dataSource;

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    private ConnectionManager() {

    }
}
