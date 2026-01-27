package com.hosttale.htlevelsystem.experience.events;

import java.util.UUID;

/**
 * Fired when experience increases (delta > 0).
 */
public final class ExperienceGainEvent extends ExperienceChangeEvent {
    public ExperienceGainEvent(UUID playerId, long previousExperience, long newExperience, int previousLevel, int newLevel) {
        super(playerId, previousExperience, newExperience, previousLevel, newLevel);
    }
}
