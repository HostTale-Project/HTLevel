package com.hosttale.htlevelsystem.experience;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Public XP API exposed by the plugin.
 * Other developers can depend on this interface to add new XP sources or read player progression.
 */
public interface ExperienceService {

    /**
     * Get a player's XP/level (creates a record if missing).
     */
    ExperienceSnapshot getOrCreate(UUID playerId);

    /**
     * Read XP/level if it exists without creating a new row.
     */
    Optional<ExperienceSnapshot> find(UUID playerId);

    /**
     * Add (or subtract) experience and fire events.
     */
    ExperienceSnapshot addExperience(UUID playerId, long delta);

    /**
     * Set absolute experience and fire events.
     */
    ExperienceSnapshot setExperience(UUID playerId, long absolute);

    /**
     * Bulk fetch snapshots for the provided players. Missing records are created.
     */
    Map<UUID, ExperienceSnapshot> getMany(Collection<UUID> playerIds);

    /**
     * Resolve the level from a raw experience amount using the configured formula.
     */
    int levelFor(long experience);

    /**
     * Minimum total XP required to reach the given level.
     */
    long experienceForLevel(int level);

    /**
     * Configured maximum level ( {@literal <=} 0 means no cap).
     */
    int getMaxLevel();
}
