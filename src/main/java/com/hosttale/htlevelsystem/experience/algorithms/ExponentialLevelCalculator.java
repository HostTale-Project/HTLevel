package com.hosttale.htlevelsystem.experience.algorithms;

/**
 * Exponential cumulative XP: base * (multiplier^L - 1) / (multiplier - 1)
 */
public final class ExponentialLevelCalculator implements LevelCalculator {
    private final long base;
    private final double multiplier;

    public ExponentialLevelCalculator(long base, double multiplier) {
        this.base = Math.max(1, base);
        // enforce a growth > 1 to avoid divide-by-zero and meaningless curves
        this.multiplier = Math.max(multiplier, 1.0001d);
    }

    @Override
    public int levelFor(long experience) {
        if (experience <= 0) {
            return 0;
        }

        double ratio = (experience * (multiplier - 1) / base) + 1.0;
        if (ratio <= 1.0) {
            return 0;
        }

        int level = (int) Math.floor(Math.log(ratio) / Math.log(multiplier));
        return Math.max(0, level);
    }

    @Override
    public long experienceForLevel(int level) {
        if (level <= 0) {
            return 0;
        }
        try {
            double value = base * (Math.pow(multiplier, level) - 1.0) / (multiplier - 1.0);
            if (Double.isInfinite(value) || value >= Long.MAX_VALUE) {
                return Long.MAX_VALUE;
            }
            return (long) Math.ceil(value);
        } catch (Exception ignored) {
            return Long.MAX_VALUE;
        }
    }
}
