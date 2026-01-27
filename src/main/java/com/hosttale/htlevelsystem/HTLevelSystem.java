package com.hosttale.htlevelsystem;

import com.hosttale.htlevelsystem.commands.HtXpCommandCollection;
import com.hosttale.htlevelsystem.config.PluginConfig;
import com.hosttale.htlevelsystem.config.PluginConfigManager;
import com.hosttale.htlevelsystem.db.DatabaseConfig;
import com.hosttale.htlevelsystem.db.DatabaseService;
import com.hosttale.htlevelsystem.db.services.MysqlDatabaseService;
import com.hosttale.htlevelsystem.db.services.PostgresDatabaseService;
import com.hosttale.htlevelsystem.db.services.SqliteDatabaseService;
import com.hosttale.htlevelsystem.experience.ExperienceService;
import com.hosttale.htlevelsystem.experience.ExperienceServiceImpl;
import com.hosttale.htlevelsystem.experience.algorithms.ExponentialLevelCalculator;
import com.hosttale.htlevelsystem.experience.algorithms.LevelCalculator;
import com.hosttale.htlevelsystem.experience.algorithms.LinearLevelCalculator;
import com.hosttale.htlevelsystem.experience.PlayerExperience;
import com.hosttale.htlevelsystem.experience.algorithms.QuadraticLevelCalculator;
import com.hypixel.hytale.event.IEventBus;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.logging.Level;

public class HTLevelSystem extends JavaPlugin {
    private DatabaseService database;
    private PluginConfigManager configManager;
    private PluginConfig pluginConfig;
    private ExperienceService experienceService;
    private LevelCalculator levelCalculator;

    public HTLevelSystem(JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        configManager = new PluginConfigManager(getDataDirectory());

        PluginConfig config;
        try {
            config = configManager.load();
        } catch (IOException e) {
            getLogger().at(Level.WARNING).withCause(e).log(
                "Failed to read htlevel.conf, falling back to defaults (sqlite + quadratic XP)"
            );
            config = new PluginConfig();
        }
        this.pluginConfig = config;

        database = createService(config.getDatabase());

        try {
            database.initialize();
            database.ensureTable(PlayerExperience.class);

            database.getStoragePath().ifPresentOrElse(
                path -> getLogger().at(Level.INFO).log("Database ready at %s (%s)", path, database.getType()),
                () -> getLogger().at(Level.INFO).log("Database ready (%s)", database.getType())
            );
            getLogger().at(Level.INFO).log("Config file: %s", configManager.getConfigPath());

            levelCalculator = createLevelCalculator(config.getExperience());
            int maxLevel = config.getExperience().getMaxLevel();
            experienceService = new ExperienceServiceImpl(database, levelCalculator, maxLevel, resolveEventBus());
            String curveInfo = describeCurve(config.getExperience(), maxLevel);
            getLogger().at(Level.INFO).log("Experience API ready %s", curveInfo);
            registerCommands();
        } catch (Exception e) {
            getLogger().at(Level.SEVERE).withCause(e).log("Failed to initialize database");
        }
    }

    private DatabaseService createService(DatabaseConfig config) {
        switch (config.getBackend()) {
            case POSTGRES:
                return new PostgresDatabaseService(config.getPostgres());
            case MYSQL:
                return new MysqlDatabaseService(config.getMysql());
            case SQLITE:
            default:
                return new SqliteDatabaseService(getDataDirectory(), config.getSqlite().getFile());
        }
    }

    @Override
    protected void shutdown() {
        if (database != null) {
            try {
                database.close();
            } catch (IOException ignored) {
                // Nothing we can do during shutdown.
            }
        }
    }

    public DatabaseService getDatabase() {
        return database;
    }

    public ExperienceService getExperienceService() {
        return experienceService;
    }

    public LevelCalculator getLevelCalculator() {
        return levelCalculator;
    }

    public PluginConfig getPluginConfig() {
        return pluginConfig;
    }

    private IEventBus resolveEventBus() {
        try {
            Object registry = getEventRegistry();
            Method getter = registry.getClass().getMethod("getEventBus");
            Object bus = getter.invoke(registry);
            if (bus instanceof IEventBus) {
                return (IEventBus) bus;
            }
        } catch (Exception ignored) {
            // Fall back to null; events will be skipped if the bus is unavailable.
        }
        return null;
    }

    private void registerCommands() {
        try {
            getCommandRegistry().registerCommand(new HtXpCommandCollection(experienceService));
            getLogger().at(Level.INFO).log("Registered admin XP commands (/htxp ...)");
        } catch (Exception e) {
            getLogger().at(Level.WARNING).withCause(e).log("Failed to register /htxp commands");
        }
    }

    private LevelCalculator createLevelCalculator(PluginConfig.ExperienceConfig xp) {
        switch (xp.getAlgorithm()) {
            case LINEAR:
                return new LinearLevelCalculator(xp.getLinear().getBasePerLevel());
            case EXPONENTIAL:
                return new ExponentialLevelCalculator(
                    xp.getExponential().getBase(),
                    xp.getExponential().getMultiplier()
                );
            case QUADRATIC:
            default:
                return new QuadraticLevelCalculator(
                    xp.getQuadratic().getBasePerLevel(),
                    xp.getQuadratic().getStep()
                );
        }
    }

    private String describeCurve(PluginConfig.ExperienceConfig xp, int maxLevel) {
        String max = (maxLevel > 0) ? ("maxLevel=" + maxLevel) : "maxLevel=unlimited";
        switch (xp.getAlgorithm()) {
            case LINEAR:
                return String.format("(curve: linear base=%d, %s)", xp.getLinear().getBasePerLevel(), max);
            case EXPONENTIAL:
                return String.format("(curve: exponential base=%d multiplier=%.3f, %s)",
                    xp.getExponential().getBase(),
                    xp.getExponential().getMultiplier(),
                    max);
            case QUADRATIC:
            default:
                return String.format("(curve: quadratic base=%d step=%d, %s)",
                    xp.getQuadratic().getBasePerLevel(),
                    xp.getQuadratic().getStep(),
                    max);
        }
    }
}
