package com.hosttale.htlevelsystem.db.services;

import com.hosttale.htlevelsystem.db.DatabaseService;
import com.hosttale.htlevelsystem.db.DatabaseType;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;

/**
 * SQLite implementation of DatabaseService. Other DBs can supply their own
 * DatabaseService without changing callers.
 */
public final class SqliteDatabaseService implements DatabaseService {
    private final Path dbPath;
    private String jdbcUrl;
    private ConnectionSource connectionSource;

    public SqliteDatabaseService(Path dataDirectory, String fileName) {
        Path candidate = Path.of(fileName);
        this.dbPath = candidate.isAbsolute() ? candidate : dataDirectory.resolve(candidate);
    }

    @Override
    public void initialize() throws IOException, SQLException {
        Files.createDirectories(dbPath.getParent());
        if (Files.notExists(dbPath)) {
            Files.createFile(dbPath);
        }

        jdbcUrl = "jdbc:sqlite:" + dbPath.toAbsolutePath();
        connectionSource = new JdbcConnectionSource(jdbcUrl);
    }

    @Override
    public ConnectionSource getConnectionSource() {
        ensureInitialized();
        return connectionSource;
    }

    @Override
    public Connection openConnection() throws SQLException {
        ensureInitialized();
        return DriverManager.getConnection(jdbcUrl);
    }

    @Override
    public <T, ID> Dao<T, ID> daoFor(Class<T> tableClass) throws SQLException {
        ensureInitialized();
        return DaoManager.createDao(connectionSource, tableClass);
    }

    @Override
    public <T> void ensureTable(Class<T> tableClass) throws SQLException {
        ensureInitialized();
        TableUtils.createTableIfNotExists(connectionSource, tableClass);
    }

    @Override
    public Optional<Path> getStoragePath() {
        return Optional.of(dbPath);
    }

    @Override
    public DatabaseType getType() {
        return DatabaseType.SQLITE;
    }

    private void ensureInitialized() {
        if (connectionSource == null) {
            throw new IllegalStateException("DatabaseService has not been initialized yet.");
        }
    }

    @Override
    public void close() throws IOException {
        if (connectionSource != null) {
            try {
                connectionSource.close();
            } catch (Exception e) {
                throw new IOException("Failed to close SQLite connection source", e);
            }
        }
    }
}
