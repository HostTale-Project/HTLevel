package com.hosttale.htlevelsystem.experience.events;

import java.util.UUID;

/**
 * Fired when a player's level decreases (e.g., after losing XP).
 */
public final class LevelDownEvent extends ExperienceChangeEvent {
    public LevelDownEvent(UUID playerId, long previousExperience, long newExperience, int previousLevel, int newLevel) {
        super(playerId, previousExperience, newExperience, previousLevel, newLevel);
    }

    public int getLevelsLost() {
        return getPreviousLevel() - getNewLevel();
    }
}
