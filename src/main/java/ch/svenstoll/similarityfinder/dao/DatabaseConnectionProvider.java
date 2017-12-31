package ch.svenstoll.similarityfinder.dao;

import ch.svenstoll.similarityfinder.domain.Settings;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * This class provides connections to the database that is used by the application.
 */
public class DatabaseConnectionProvider {
    @NotNull
    private final String jdbcDriver;
    @NotNull
    private final Settings settings;

    /**
     * Constructs a {@code DatabaseConnectionProvider}.
     *
     * @param jdbcDriver the JDBC driver to be used to communicate with the database
     * @param settings the {@code Settings} used throughout the application
     * @throws IllegalArgumentException if any parameter was {@code null}
     */
    @Inject
    public DatabaseConnectionProvider(@NotNull @Named("JDBC_DRIVER") String jdbcDriver,
                                      @NotNull Settings settings) {
        this.settings = Validate.notNull(settings, "Settings must not be null.");
        this.jdbcDriver = Validate.notNull(jdbcDriver, "JdbcDriver must not be null.");
    }

    /**
     * Returns a new database connection using the credentials stored in the application settings.
     *
     * @return an open database connection
     * @throws SQLException if an error occurred while opening a new database connection
     */
    public @NotNull Connection getDbConnection() throws SQLException {
        return DriverManager.getConnection(jdbcDriver + settings.getDbAddress(),
                settings.getDbUser(), settings.getDbPassword());
    }
}
