package com.example.liftlog.ui.onerm;

public final class OneRmCalculator {

    public enum Formula {
        BRZYCKI, EPLEY, LANDER
    }

    private OneRmCalculator() { }

    /**
     * Oblicza szacowane 1RM. Wszystkie trzy formuły mają sensowne wyniki dla 1..10 powtórzeń,
     * powyżej dokładność spada.
     */
    public static float compute(Formula formula, float weightKg, int reps) {
        if (reps <= 0) return 0f;
        if (reps == 1) return weightKg;
        switch (formula) {
            case BRZYCKI:
                // W / (1.0278 - 0.0278 * R) — niewystarczająco dokładne powyżej R~10
                return (float) (weightKg / (1.0278 - 0.0278 * reps));
            case EPLEY:
                return (float) (weightKg * (1 + reps / 30.0));
            case LANDER:
                return (float) ((100.0 * weightKg) / (101.3 - 2.67123 * reps));
            default:
                return 0f;
        }
    }
}
