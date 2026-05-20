package com.example.liftlog.ui.workout;

import android.app.Application;
import android.os.CountDownTimer;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.liftlog.data.AppDatabase;
import com.example.liftlog.data.dao.ExerciseDao;
import com.example.liftlog.data.model.Exercise;
import com.example.liftlog.data.model.PlanExercise;
import com.example.liftlog.data.model.SessionSet;
import com.example.liftlog.data.model.TrainingPlan;
import com.example.liftlog.logic.ActiveWorkoutManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ActiveWorkoutViewModel extends AndroidViewModel {

    public static class ExerciseEntry {
        public final int exerciseId;
        public final String name;
        public final int setsTarget;

        public ExerciseEntry(int exerciseId, String name, int setsTarget) {
            this.exerciseId = exerciseId;
            this.name = name;
            this.setsTarget = setsTarget;
        }
    }

    private final ActiveWorkoutManager manager;
    private final ExerciseDao exerciseDao;
    private final AppDatabase db;

    private final MutableLiveData<List<ExerciseEntry>> exercises = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<List<SessionSet>> allSets = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Integer> restRemaining = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> finished = new MutableLiveData<>(false);

    private CountDownTimer timer;
    private int planId = -1;
    private boolean initialized = false;

    public ActiveWorkoutViewModel(@NonNull Application application) {
        super(application);
        manager = ActiveWorkoutManager.getInstance(application);
        db = AppDatabase.getInstance(application);
        exerciseDao = db.exerciseDao();
    }

    public LiveData<List<ExerciseEntry>> getExercises() { return exercises; }
    public LiveData<List<SessionSet>> getAllSets() { return allSets; }
    public LiveData<Integer> getRestRemaining() { return restRemaining; }
    public LiveData<Boolean> getFinished() { return finished; }
    public LiveData<List<Exercise>> getAllExercisesForPicker() { return exerciseDao.getAllExercises(); }
    public boolean isQuickStart() { return planId <= 0; }

    public void initialize(int planId) {
        if (initialized) return;
        initialized = true;
        this.planId = planId;
        AppDatabase.DB_EXECUTOR.execute(() -> {
            manager.discardWorkout();
            allSets.postValue(Collections.emptyList());
            if (planId > 0) {
                TrainingPlan plan = db.trainingPlanDao().getById(planId);
                manager.startWorkout(plan);
                List<PlanExercise> pes = db.planExerciseDao().getExercisesForPlanSync(planId);
                List<ExerciseEntry> entries = new ArrayList<>(pes.size());
                for (PlanExercise pe : pes) {
                    Exercise ex = exerciseDao.getById(pe.exerciseId);
                    entries.add(new ExerciseEntry(pe.exerciseId,
                            ex == null ? "?" : ex.name, pe.sets));
                }
                exercises.postValue(entries);
            } else {
                manager.startWorkout();
                exercises.postValue(new ArrayList<>());
            }
        });
    }

    // Dla trybu quickstart: dodaje ćwiczenie do listy (nie zastępuje)
    public void addPickedExercise(int exerciseId) {
        AppDatabase.DB_EXECUTOR.execute(() -> {
            Exercise ex = exerciseDao.getById(exerciseId);
            if (ex == null) return;
            List<ExerciseEntry> current = exercises.getValue();
            if (current == null) current = new ArrayList<>();
            // Nie dodawaj duplikatów
            for (ExerciseEntry e : current) {
                if (e.exerciseId == exerciseId) return;
            }
            List<ExerciseEntry> updated = new ArrayList<>(current);
            updated.add(new ExerciseEntry(exerciseId, ex.name, 99));
            exercises.postValue(updated);
        });
    }

    // exerciseId podany jawnie — setNumber liczony na podstawie allSets
    public void deleteSet(SessionSet set) {
        manager.removeSet(set);
        allSets.setValue(new ArrayList<>(manager.getCurrentSets()));
    }

    public void setExercisesOrder(List<ExerciseEntry> ordered) {
        exercises.setValue(new ArrayList<>(ordered));
    }

    public void logSet(int exerciseId, float weightKg, int repsDone) {
        List<SessionSet> current = allSets.getValue();
        int setNo = 1;
        if (current != null) {
            for (SessionSet s : current) {
                if (s.exerciseId == exerciseId) setNo++;
            }
        }
        manager.logSet(exerciseId, setNo, weightKg, repsDone);
        allSets.setValue(new ArrayList<>(manager.getCurrentSets()));
    }

    public void startRestTimer(int seconds) {
        cancelRestTimer();
        restRemaining.setValue(seconds);
        timer = new CountDownTimer(seconds * 1000L, 1000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                restRemaining.setValue((int) (millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                restRemaining.setValue(0);
            }
        }.start();
    }

    public void cancelRestTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        restRemaining.setValue(0);
    }

    public void finishWorkout(String notes) {
        cancelRestTimer();
        manager.finishWorkout(notes);
        finished.postValue(true);
    }

    public void discardWorkout() {
        cancelRestTimer();
        manager.discardWorkout();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        cancelRestTimer();
    }
}
