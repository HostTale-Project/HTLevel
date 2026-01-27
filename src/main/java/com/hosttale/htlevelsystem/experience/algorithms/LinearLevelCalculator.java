package com.hosttale.htlevelsystem.experience.algorithms;

/**
 * Linear XP curve: total xp = basePerLevel * level.
 */
public final class LinearLevelCalculator implements LevelCalculator {
    private final long basePerLevel;

    public LinearLevelCalculator(long basePerLevel) {
        this.basePerLevel = Math.max(1, basePerLevel);
    }

    @Override
    public int levelFor(long experience) {
        if (experience <= 0) {
            return 0;
        }
        return (int) Math.floor((double) experience / basePerLevel);
    }

    @Override
    public long experienceForLevel(int level) {
        if (level <= 0) {
            return 0;
        }
        try {
            return Math.multiplyExact(basePerLevel, level);
        } catch (ArithmeticException ignored) {
            return Long.MAX_VALUE;
        }
    }
}
