package com.hosttale.htlevelsystem.experience.algorithms;

/**
 * Simple quadratic XP curve: xp = base * level + step * level^2
 * - Level 0 requires 0 XP.
 * - Level 1 requires base + step XP, etc.
 */
public final class QuadraticLevelCalculator implements LevelCalculator {
    private final long basePerLevel;
    private final long step;

    public QuadraticLevelCalculator(long basePerLevel, long step) {
        this.basePerLevel = Math.max(0, basePerLevel);
        this.step = Math.max(1, step);
    }

    @Override
    public int levelFor(long experience) {
        if (experience <= 0) {
            return 0;
        }

        // Solve quadratic: step*l^2 + base*l - experience <= 0
        double a = step;
        double b = basePerLevel;
        double c = -experience;

        int level = (int) Math.floor((-b + Math.sqrt(b * b - 4 * a * c)) / (2 * a));
        return Math.max(0, level);
    }

    @Override
    public long experienceForLevel(int level) {
        if (level <= 0) {
            return 0;
        }
        long l = level;
        try {
            long linear = Math.multiplyExact(basePerLevel, l);
            long quad = Math.multiplyExact(step, Math.multiplyExact(l, l));
            return Math.addExact(linear, quad);
        } catch (ArithmeticException ignored) {
            return Long.MAX_VALUE;
        }
    }
}
