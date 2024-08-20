package utils;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public final class ConnectionManager {
    private static HikariDataSource dataSource;
    public static void setDataSource(HikariDataSource ds) {
        dataSource = ds;
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    private ConnectionManager() {

    }
}
