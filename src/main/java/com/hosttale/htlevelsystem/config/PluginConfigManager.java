package com.hosttale.htlevelsystem.config;

import com.hosttale.htlevelsystem.db.DatabaseConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.Properties;

/**
 * Loads and writes the plugin configuration (database + experience).
 * Uses a simple .conf file with comments for clarity.
 */
public final class PluginConfigManager {
    private static final String FILE_NAME = "htlevel.conf";
    private static final String LEGACY_FILE_NAME = "database.conf";

    private final Path configPath;

    public PluginConfigManager(Path dataDirectory) {
        this.configPath = dataDirectory.resolve(FILE_NAME);
    }

    public Path getConfigPath() {
        return configPath;
    }

    public PluginConfig load() throws IOException {
        if (Files.notExists(configPath)) {
            migrateLegacyIfPresent();
            if (Files.notExists(configPath)) {
                writeDefaultTemplate();
            }
        }

        Properties props = new Properties();
        try (BufferedReader reader = Files.newBufferedReader(configPath, StandardCharsets.UTF_8)) {
            props.load(reader);
        }

        PluginConfig config = new PluginConfig();
        hydrateDatabase(config.getDatabase(), props);
        hydrateExperience(config.getExperience(), props);
        return config;
    }

    private void hydrateDatabase(DatabaseConfig config, Properties props) {
        String backendRaw = props.getProperty("backend", "sqlite").toLowerCase(Locale.ROOT);
        switch (backendRaw) {
            case "postgres":
            case "postgresql":
                config.setBackend(DatabaseConfig.Backend.POSTGRES);
                break;
            case "mysql":
                config.setBackend(DatabaseConfig.Backend.MYSQL);
                break;
            default:
                config.setBackend(DatabaseConfig.Backend.SQLITE);
                break;
        }

        // SQLite
        config.getSqlite().setFile(props.getProperty("sqlite.file", config.getSqlite().getFile()));

        // Postgres
        config.getPostgres().setHost(props.getProperty("postgres.host", config.getPostgres().getHost()));
        config.getPostgres().setPort(parseInt(props.getProperty("postgres.port"), config.getPostgres().getPort()));
        config.getPostgres().setDatabase(props.getProperty("postgres.database", config.getPostgres().getDatabase()));
        config.getPostgres().setUser(props.getProperty("postgres.user", config.getPostgres().getUser()));
        config.getPostgres().setPassword(props.getProperty("postgres.password", config.getPostgres().getPassword()));
        config.getPostgres().setSsl(parseBool(props.getProperty("postgres.ssl"), config.getPostgres().isSsl()));

        // MySQL
        config.getMysql().setHost(props.getProperty("mysql.host", config.getMysql().getHost()));
        config.getMysql().setPort(parseInt(props.getProperty("mysql.port"), config.getMysql().getPort()));
        config.getMysql().setDatabase(props.getProperty("mysql.database", config.getMysql().getDatabase()));
        config.getMysql().setUser(props.getProperty("mysql.user", config.getMysql().getUser()));
        config.getMysql().setPassword(props.getProperty("mysql.password", config.getMysql().getPassword()));
        config.getMysql().setUseSsl(parseBool(props.getProperty("mysql.useSSL"), config.getMysql().isUseSsl()));
    }

    private void hydrateExperience(PluginConfig.ExperienceConfig config, Properties props) {
        String algorithmRaw = props.getProperty("xp.algorithm", config.getAlgorithm().name()).toUpperCase(Locale.ROOT);
        try {
            config.setAlgorithm(PluginConfig.ExperienceConfig.Algorithm.valueOf(algorithmRaw));
        } catch (IllegalArgumentException ignored) {
            config.setAlgorithm(PluginConfig.ExperienceConfig.Algorithm.QUADRATIC);
        }

        config.setMaxLevel(parseInt(props.getProperty("xp.maxLevel"), config.getMaxLevel()));

        // Quadratic
        config.getQuadratic().setBasePerLevel(
            parseLong(props.getProperty("xp.quadratic.basePerLevel"), config.getQuadratic().getBasePerLevel())
        );
        config.getQuadratic().setStep(
            parseLong(props.getProperty("xp.quadratic.step"), config.getQuadratic().getStep())
        );

        // Linear
        config.getLinear().setBasePerLevel(
            parseLong(props.getProperty("xp.linear.basePerLevel"), config.getLinear().getBasePerLevel())
        );

        // Exponential
        config.getExponential().setBase(
            parseLong(props.getProperty("xp.exponential.base"), config.getExponential().getBase())
        );
        config.getExponential().setMultiplier(
            parseDouble(props.getProperty("xp.exponential.multiplier"), config.getExponential().getMultiplier())
        );
    }

    private int parseInt(String raw, int defaultValue) {
        if (raw == null) return defaultValue;
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }

    private long parseLong(String raw, long defaultValue) {
        if (raw == null) return defaultValue;
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }

    private double parseDouble(String raw, double defaultValue) {
        if (raw == null) return defaultValue;
        try {
            return Double.parseDouble(raw.trim());
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }

    private boolean parseBool(String raw, boolean defaultValue) {
        if (raw == null) return defaultValue;
        return Boolean.parseBoolean(raw.trim());
    }

    private void writeDefaultTemplate() throws IOException {
        Files.createDirectories(configPath.getParent());
        String template = String.join(System.lineSeparator(),
            "# HTLevelSystem configuration",
            "# Database backend: sqlite | postgres | mysql",
            "backend=sqlite",
            "",
            "# SQLite settings (used when backend=sqlite)",
            "# Relative paths are resolved inside the plugin data folder",
            "sqlite.file=users.sql",
            "",
            "# PostgreSQL settings (used when backend=postgres)",
            "postgres.host=localhost",
            "postgres.port=5432",
            "postgres.database=hytale",
            "postgres.user=hytale",
            "postgres.password=change_me",
            "postgres.ssl=false",
            "",
            "# MySQL settings (used when backend=mysql)",
            "mysql.host=localhost",
            "mysql.port=3306",
            "mysql.database=hytale",
            "mysql.user=hytale",
            "mysql.password=change_me",
            "mysql.useSSL=false",
            "",
            "# Experience/level settings",
            "# algorithm: quadratic | linear | exponential",
            "xp.algorithm=quadratic",
            "# Max level (<=0 means no cap)",
            "xp.maxLevel=100",
            "",
            "# Quadratic: xp = basePerLevel * L + step * L^2",
            "xp.quadratic.basePerLevel=100",
            "xp.quadratic.step=25",
            "",
            "# Linear: xp = basePerLevel * L",
            "xp.linear.basePerLevel=150",
            "",
            "# Exponential cumulative: base * (multiplier^L - 1) / (multiplier - 1)",
            "xp.exponential.base=100",
            "xp.exponential.multiplier=1.15",
            ""
        );
        Files.writeString(
            configPath,
            template,
            StandardCharsets.UTF_8,
            StandardOpenOption.CREATE_NEW
        );
    }

    /**
     * If an older database.conf exists and the new file does not, migrate it forward.
     */
    private void migrateLegacyIfPresent() throws IOException {
        Path legacy = configPath.getParent().resolve(LEGACY_FILE_NAME);
        if (Files.exists(legacy) && Files.notExists(configPath)) {
            Files.createDirectories(configPath.getParent());
            Files.copy(legacy, configPath);
        }
    }
}
