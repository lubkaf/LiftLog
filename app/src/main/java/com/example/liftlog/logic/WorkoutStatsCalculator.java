package com.example.liftlog.logic;

import com.example.liftlog.data.model.SessionSet;

import java.util.List;

public class WorkoutStatsCalculator {

    public static float calculateVolume(List<SessionSet> sets) {
        float volume = 0;
        for (SessionSet s : sets) {
            volume += s.weightKg * s.repsDone;
        }
        return volume;
    }

    public static float getMaxWeightInSession(List<SessionSet> sets, int exerciseId) {
        float max = 0;
        for (SessionSet s : sets) {
            if (s.exerciseId == exerciseId && s.weightKg > max) {
                max = s.weightKg;
            }
        }
        return max;
    }

    public static boolean isPersonalRecord(float currentMax, float previousRecord) {
        return currentMax > previousRecord;
    }
}
