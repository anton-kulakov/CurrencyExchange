package servlet;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import utils.ConnectionManager;

public class AppContextListener implements ServletContextListener {
    private HikariDataSource dataSource;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.out.println("Failed to load SQLite JDBC driver");
        }

        HikariConfig config = new HikariConfig("/database.properties");
        dataSource = new HikariDataSource(config);

        ConnectionManager.setDataSource(dataSource);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
