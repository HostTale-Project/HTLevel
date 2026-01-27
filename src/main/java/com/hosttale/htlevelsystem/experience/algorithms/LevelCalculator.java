package com.hosttale.htlevelsystem.experience.algorithms;

/**
 * Strategy that converts raw experience points into a level (and back).
 * Implementations define the curve; callers can swap the calculator without
 * touching the rest of the XP system.
 */
public interface LevelCalculator {

    /**
     * Convert experience to the corresponding level.
     */
    int levelFor(long experience);

    /**
     * Minimum cumulative experience required to reach the requested level.
     */
    long experienceForLevel(int level);
}
