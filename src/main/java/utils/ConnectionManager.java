package utils;

import com.zaxxer.hikari.HikariDataSource;
import exception.DBException;
import lombok.Setter;

import java.sql.Connection;
import java.sql.SQLException;

import static jakarta.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

public final class ConnectionManager {
    @Setter
    private static HikariDataSource dataSource;

    public static Connection getConnection() throws DBException {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new DBException(SC_INTERNAL_SERVER_ERROR, "Something happened with the database. Please try again later!");
        }
    }

    private ConnectionManager() {

    }
}
