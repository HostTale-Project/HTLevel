package com.hosttale.htlevelsystem.experience.events;

import java.util.UUID;

/**
 * Fired when a player's level increases.
 */
public final class LevelUpEvent extends ExperienceChangeEvent {
    public LevelUpEvent(UUID playerId, long previousExperience, long newExperience, int previousLevel, int newLevel) {
        super(playerId, previousExperience, newExperience, previousLevel, newLevel);
    }

    public int getLevelsGained() {
        return getNewLevel() - getPreviousLevel();
    }
}
