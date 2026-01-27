package com.hosttale.htlevelsystem.experience;

import com.hosttale.htlevelsystem.db.DatabaseService;
import com.hosttale.htlevelsystem.experience.algorithms.LevelCalculator;
import com.hosttale.htlevelsystem.experience.events.ExperienceChangeEvent;
import com.hosttale.htlevelsystem.experience.events.ExperienceGainEvent;
import com.hosttale.htlevelsystem.experience.events.ExperienceLossEvent;
import com.hosttale.htlevelsystem.experience.events.LevelDownEvent;
import com.hosttale.htlevelsystem.experience.events.LevelUpEvent;
import com.hypixel.hytale.event.IEventBus;
import com.hypixel.hytale.event.IEventDispatcher;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Concrete ExperienceService backed by ORMLite + the plugin database service.
 */
public final class ExperienceServiceImpl implements ExperienceService {
    private final Dao<PlayerExperience, UUID> dao;
    private final LevelCalculator levelCalculator;
    private final IEventBus eventBus;
    private final int maxLevel;
    private final long maxExperience;

    public ExperienceServiceImpl(DatabaseService database, LevelCalculator levelCalculator, int maxLevel, IEventBus eventBus) throws SQLException {
        this.dao = database.daoFor(PlayerExperience.class);
        this.levelCalculator = levelCalculator;
        this.eventBus = eventBus;
        this.maxLevel = maxLevel;
        long cap = (maxLevel > 0) ? levelCalculator.experienceForLevel(maxLevel) : Long.MAX_VALUE;
        this.maxExperience = cap > 0 ? cap : Long.MAX_VALUE;
    }

    @Override
    public ExperienceSnapshot getOrCreate(UUID playerId) {
        PlayerExperience record = loadOrCreate(playerId);
        return toSnapshot(record);
    }

    @Override
    public Optional<ExperienceSnapshot> find(UUID playerId) {
        try {
            PlayerExperience record = dao.queryForId(playerId);
            return Optional.ofNullable(record).map(this::toSnapshot);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load experience for player " + playerId, e);
        }
    }

    @Override
    public ExperienceSnapshot addExperience(UUID playerId, long delta) {
        PlayerExperience record = loadOrCreate(playerId);

        long previousXp = record.getExperience();
        int previousLevel = levelFor(previousXp);

        record.addExperience(delta);
        applyCap(record);
        persist(record);

        long newXp = record.getExperience();
        int newLevel = levelFor(newXp);

        if (newXp == previousXp) {
            return new ExperienceSnapshot(record.getPlayerId(), newXp, newLevel);
        }

        fireEvents(record.getPlayerId(), previousXp, newXp, previousLevel, newLevel);
        return new ExperienceSnapshot(record.getPlayerId(), newXp, newLevel);
    }

    @Override
    public ExperienceSnapshot setExperience(UUID playerId, long absolute) {
        PlayerExperience record = loadOrCreate(playerId);

        long previousXp = record.getExperience();
        int previousLevel = levelFor(previousXp);

        record.setExperience(absolute);
        applyCap(record);
        persist(record);

        long newXp = record.getExperience();
        int newLevel = levelFor(newXp);

        if (newXp == previousXp) {
            return new ExperienceSnapshot(record.getPlayerId(), newXp, newLevel);
        }

        fireEvents(record.getPlayerId(), previousXp, newXp, previousLevel, newLevel);
        return new ExperienceSnapshot(record.getPlayerId(), newXp, newLevel);
    }

    @Override
    public Map<UUID, ExperienceSnapshot> getMany(Collection<UUID> playerIds) {
        Map<UUID, ExperienceSnapshot> result = new HashMap<>();
        for (UUID playerId : playerIds) {
            PlayerExperience record = loadOrCreate(playerId);
            result.put(playerId, toSnapshot(record));
        }
        return result;
    }

    @Override
    public int levelFor(long experience) {
        long capped = Math.min(Math.max(0, experience), maxExperience);
        int level = levelCalculator.levelFor(capped);
        if (maxLevel > 0) {
            level = Math.min(level, maxLevel);
        }
        return level;
    }

    @Override
    public long experienceForLevel(int level) {
        if (maxLevel > 0 && level > maxLevel) {
            return maxExperience;
        }
        return levelCalculator.experienceForLevel(level);
    }

    @Override
    public int getMaxLevel() {
        return maxLevel;
    }

    private ExperienceSnapshot toSnapshot(PlayerExperience record) {
        long xp = Math.min(record.getExperience(), maxExperience);
        int level = levelFor(xp);
        return new ExperienceSnapshot(record.getPlayerId(), xp, level);
    }

    private PlayerExperience loadOrCreate(UUID playerId) {
        try {
            PlayerExperience existing = dao.queryForId(playerId);
            if (existing != null) {
                if (applyCap(existing)) {
                    persist(existing);
                }
                return existing;
            }
            PlayerExperience created = new PlayerExperience(playerId, 0);
            dao.create(created);
            return created;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load or create experience row for " + playerId, e);
        }
    }

    private void persist(PlayerExperience record) {
        try {
            dao.createOrUpdate(record);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to persist experience for " + record.getPlayerId(), e);
        }
    }

    private boolean applyCap(PlayerExperience record) {
        if (maxLevel <= 0) {
            return false;
        }
        long capped = Math.min(record.getExperience(), maxExperience);
        if (capped < 0) {
            capped = 0;
        }
        if (capped != record.getExperience()) {
            record.setExperience(capped);
            return true;
        }
        return false;
    }

    private void fireEvents(UUID playerId, long previousXp, long newXp, int previousLevel, int newLevel) {
        ExperienceChangeEvent base = new ExperienceChangeEvent(playerId, previousXp, newXp, previousLevel, newLevel);
        dispatch(base);

        if (newXp > previousXp) {
            dispatch(new ExperienceGainEvent(playerId, previousXp, newXp, previousLevel, newLevel));
        } else if (newXp < previousXp) {
            dispatch(new ExperienceLossEvent(playerId, previousXp, newXp, previousLevel, newLevel));
        }

        if (newLevel > previousLevel) {
            dispatch(new LevelUpEvent(playerId, previousXp, newXp, previousLevel, newLevel));
        } else if (newLevel < previousLevel) {
            dispatch(new LevelDownEvent(playerId, previousXp, newXp, previousLevel, newLevel));
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void dispatch(ExperienceChangeEvent event) {
        if (eventBus == null) {
            return;
        }
        try {
            IEventDispatcher dispatcher = eventBus.dispatchFor(event.getClass(), event.getHolder());
            dispatcher.dispatch(event);
        } catch (Exception ignored) {
            // Avoid breaking gameplay if the event bus misbehaves.
        }
    }
}
