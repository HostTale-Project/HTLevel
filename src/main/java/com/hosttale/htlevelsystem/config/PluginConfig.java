package com.hosttale.htlevelsystem.config;

import com.hosttale.htlevelsystem.db.DatabaseConfig;

/**
 * Root configuration for the plugin: database + experience settings.
 */
public final class PluginConfig {
    private final DatabaseConfig database = DatabaseConfig.defaults();
    private final ExperienceConfig experience = ExperienceConfig.defaults();

    public DatabaseConfig getDatabase() {
        return database;
    }

    public ExperienceConfig getExperience() {
        return experience;
    }

    public static final class ExperienceConfig {
        public enum Algorithm { QUADRATIC, LINEAR, EXPONENTIAL }

        private Algorithm algorithm = Algorithm.QUADRATIC;
        /**
         * Max attainable level. Set <= 0 for no cap.
         */
        private int maxLevel = 100;
        private Quadratic quadratic = new Quadratic();
        private Linear linear = new Linear();
        private Exponential exponential = new Exponential();

        public Algorithm getAlgorithm() {
            return algorithm;
        }

        public void setAlgorithm(Algorithm algorithm) {
            this.algorithm = algorithm;
        }

        public int getMaxLevel() {
            return maxLevel;
        }

        public void setMaxLevel(int maxLevel) {
            this.maxLevel = maxLevel;
        }

        public Quadratic getQuadratic() {
            return quadratic;
        }

        public Linear getLinear() {
            return linear;
        }

        public Exponential getExponential() {
            return exponential;
        }

        public static ExperienceConfig defaults() {
            return new ExperienceConfig();
        }

        public static final class Quadratic {
            private long basePerLevel = 100;
            private long step = 25;

            public long getBasePerLevel() {
                return basePerLevel;
            }

            public void setBasePerLevel(long basePerLevel) {
                this.basePerLevel = basePerLevel;
            }

            public long getStep() {
                return step;
            }

            public void setStep(long step) {
                this.step = step;
            }
        }

        public static final class Linear {
            private long basePerLevel = 150;

            public long getBasePerLevel() {
                return basePerLevel;
            }

            public void setBasePerLevel(long basePerLevel) {
                this.basePerLevel = basePerLevel;
            }
        }

        public static final class Exponential {
            private long base = 100;
            private double multiplier = 1.15;

            public long getBase() {
                return base;
            }

            public void setBase(long base) {
                this.base = base;
            }

            public double getMultiplier() {
                return multiplier;
            }

            public void setMultiplier(double multiplier) {
                this.multiplier = multiplier;
            }
        }
    }
}
