package com.example.liftlog.logic;

import android.content.Context;

import com.example.liftlog.data.model.SessionSet;
import com.example.liftlog.data.model.TrainingPlan;
import com.example.liftlog.data.model.WorkoutSession;
import com.example.liftlog.data.repository.WorkoutSessionRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ActiveWorkoutManager {

    private static volatile ActiveWorkoutManager INSTANCE;

    private final WorkoutSessionRepository repository;

    private Integer activePlanId;
    private long startTimeMillis;
    private final List<SessionSet> pendingSets = new ArrayList<>();
    private boolean isActive = false;

    private ActiveWorkoutManager(Context context) {
        repository = new WorkoutSessionRepository(context.getApplicationContext());
    }

    public static ActiveWorkoutManager getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (ActiveWorkoutManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ActiveWorkoutManager(context);
                }
            }
        }
        return INSTANCE;
    }

    public void startWorkout(TrainingPlan plan) {
        activePlanId = (plan != null) ? plan.id : null;
        startTimeMillis = System.currentTimeMillis();
        pendingSets.clear();
        isActive = true;
    }

    public void startWorkout() {
        startWorkout(null);
    }

    public void logSet(int exerciseId, int setNumber, float weightKg, int repsDone) {
        if (!isActive) return;
        SessionSet set = new SessionSet();
        set.exerciseId = exerciseId;
        set.setNumber = setNumber;
        set.weightKg = weightKg;
        set.repsDone = repsDone;
        pendingSets.add(set);
    }

    public void finishWorkout(String notes) {
        if (!isActive) return;

        long endTimeMillis = System.currentTimeMillis();
        int durationMinutes = (int) ((endTimeMillis - startTimeMillis) / 60_000);

        WorkoutSession session = new WorkoutSession();
        session.planId = activePlanId;
        session.date = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        session.durationMinutes = durationMinutes;
        session.notes = notes;

        List<SessionSet> setsToSave = new ArrayList<>(pendingSets);
        repository.saveCompletedSession(session, setsToSave);

        discardWorkout();
    }

    public void discardWorkout() {
        activePlanId = null;
        startTimeMillis = 0;
        pendingSets.clear();
        isActive = false;
    }

    public List<SessionSet> getCurrentSets() {
        return Collections.unmodifiableList(pendingSets);
    }

    public boolean isActive() {
        return isActive;
    }

    public long getStartTimeMillis() {
        return startTimeMillis;
    }

    public long getElapsedMillis() {
        return isActive ? System.currentTimeMillis() - startTimeMillis : 0;
    }
}
