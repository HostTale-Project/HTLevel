package com.hosttale.htlevelsystem.db.services;

import com.hosttale.htlevelsystem.db.DatabaseConfig;
import com.hosttale.htlevelsystem.db.DatabaseService;
import com.hosttale.htlevelsystem.db.DatabaseType;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;

public final class PostgresDatabaseService implements DatabaseService {
    private final DatabaseConfig.Postgres config;
    private String jdbcUrl;
    private ConnectionSource connectionSource;

    public PostgresDatabaseService(DatabaseConfig.Postgres config) {
        this.config = config;
    }

    @Override
    public void initialize() throws SQLException {
        jdbcUrl = String.format(
            "jdbc:postgresql://%s:%d/%s?sslmode=%s",
            config.getHost(),
            config.getPort(),
            config.getDatabase(),
            config.isSsl() ? "require" : "disable"
        );
        connectionSource = new JdbcConnectionSource(jdbcUrl, config.getUser(), config.getPassword());
    }

    @Override
    public ConnectionSource getConnectionSource() {
        ensureInitialized();
        return connectionSource;
    }

    @Override
    public Connection openConnection() throws SQLException {
        ensureInitialized();
        return DriverManager.getConnection(jdbcUrl, config.getUser(), config.getPassword());
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
    public Optional<java.nio.file.Path> getStoragePath() {
        return Optional.empty();
    }

    @Override
    public DatabaseType getType() {
        return DatabaseType.POSTGRES;
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
                throw new IOException("Failed to close Postgres connection source", e);
            }
        }
    }
}
