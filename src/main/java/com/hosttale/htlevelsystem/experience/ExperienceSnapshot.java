package com.hosttale.htlevelsystem.experience;

import java.util.UUID;

/**
 * Immutable view of a player's XP + level at a point in time.
 */
public record ExperienceSnapshot(UUID playerId, long experience, int level) {
}
