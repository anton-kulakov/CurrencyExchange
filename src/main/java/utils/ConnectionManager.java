package utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public final class ConnectionManager {
    private static HikariDataSource dataSource;
    static {
        HikariConfig config = new HikariConfig("/database.properties");
        dataSource = new HikariDataSource(config);
    }
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
    private ConnectionManager() {

    }
}
