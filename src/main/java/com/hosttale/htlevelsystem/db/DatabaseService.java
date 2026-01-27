package com.hosttale.htlevelsystem.db;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Generic database service abstraction so we can swap implementations
 * (SQLite now, Postgres/MySQL later) without touching calling code.
 */
public interface DatabaseService extends Closeable {

    /**
     * Prepare the database for use (create files or test connectivity).
     */
    void initialize() throws Exception;

    /**
     * Connection source for ORMLite.
     */
    ConnectionSource getConnectionSource();

    /**
     * Open a raw JDBC connection. Caller closes.
     */
    Connection openConnection() throws SQLException;

    /**
     * Build a DAO for the given entity class.
     */
    <T, ID> Dao<T, ID> daoFor(Class<T> tableClass) throws SQLException;

    /**
     * Create the table if it does not exist.
     */
    <T> void ensureTable(Class<T> tableClass) throws SQLException;

    /**
     * Local storage path if applicable (e.g., SQLite file). Empty for remote DBs.
     */
    Optional<Path> getStoragePath();

    /**
     * Identify the backend in use.
     */
    DatabaseType getType();

    @Override
    void close() throws IOException;
}
