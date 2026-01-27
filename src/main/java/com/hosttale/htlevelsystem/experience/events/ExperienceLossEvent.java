package com.hosttale.htlevelsystem.experience.events;

import java.util.UUID;

/**
 * Fired when experience decreases (delta {@literal <} 0).
 */
public final class ExperienceLossEvent extends ExperienceChangeEvent {
    public ExperienceLossEvent(UUID playerId, long previousExperience, long newExperience, int previousLevel, int newLevel) {
        super(playerId, previousExperience, newExperience, previousLevel, newLevel);
    }
}
