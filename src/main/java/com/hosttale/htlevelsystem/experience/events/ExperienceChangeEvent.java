package com.hosttale.htlevelsystem.experience.events;

import com.hypixel.hytale.event.IEvent;

import java.util.UUID;

/**
 * Fired whenever a player's total experience changes.
 * Holder is the player UUID, so listeners can register keyed handlers.
 */
public class ExperienceChangeEvent implements IEvent<UUID> {
    private final UUID playerId;
    private final long previousExperience;
    private final long newExperience;
    private final int previousLevel;
    private final int newLevel;

    public ExperienceChangeEvent(
        UUID playerId,
        long previousExperience,
        long newExperience,
        int previousLevel,
        int newLevel
    ) {
        this.playerId = playerId;
        this.previousExperience = previousExperience;
        this.newExperience = newExperience;
        this.previousLevel = previousLevel;
        this.newLevel = newLevel;
    }

    public UUID getHolder() {
        return playerId;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public long getPreviousExperience() {
        return previousExperience;
    }

    public long getNewExperience() {
        return newExperience;
    }

    public long getDelta() {
        return newExperience - previousExperience;
    }

    public int getPreviousLevel() {
        return previousLevel;
    }

    public int getNewLevel() {
        return newLevel;
    }
}
