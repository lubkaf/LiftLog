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
    private final MutableLiveData<Integer> currentIdx = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> currentSetNumber = new MutableLiveData<>(1);
    private final MutableLiveData<List<SessionSet>> setsForCurrent = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Integer> restRemaining = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> finished = new MutableLiveData<>(false);

    private CountDownTimer timer;
    private int pickedExerciseId = -1;
    private int planId = -1;
    private boolean initialized = false;

    public ActiveWorkoutViewModel(@NonNull Application application) {
        super(application);
        manager = ActiveWorkoutManager.getInstance(application);
        db = AppDatabase.getInstance(application);
        exerciseDao = db.exerciseDao();
    }

    public LiveData<List<ExerciseEntry>> getExercises() { return exercises; }
    public LiveData<Integer> getCurrentIdx() { return currentIdx; }
    public LiveData<Integer> getCurrentSetNumber() { return currentSetNumber; }
    public LiveData<List<SessionSet>> getSetsForCurrent() { return setsForCurrent; }
    public LiveData<Integer> getRestRemaining() { return restRemaining; }
    public LiveData<Boolean> getFinished() { return finished; }
    public LiveData<List<Exercise>> getAllExercisesForPicker() { return exerciseDao.getAllExercises(); }
    public boolean isQuickStart() { return planId <= 0; }

    public void initialize(int planId) {
        if (initialized) return;
        initialized = true;
        this.planId = planId;
        AppDatabase.DB_EXECUTOR.execute(() -> {
            // Po zmianie w ActiveWorkoutManager startWorkout jest no-op gdy isActive==true.
            // Wyczyszczamy stan na wszelki wypadek — jeśli poprzedni trening został porzucony
            // (np. force-close apki), nowa sesja musi zacząć się od zera.
            manager.discardWorkout();
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
                currentIdx.postValue(0);
                currentSetNumber.postValue(1);
                refreshSetsForCurrent(entries, 0);
            } else {
                manager.startWorkout();
                exercises.postValue(Collections.emptyList());
                currentIdx.postValue(0);
                currentSetNumber.postValue(1);
                setsForCurrent.postValue(Collections.emptyList());
            }
        });
    }

    public void setPickedExercise(int exerciseId) {
        pickedExerciseId = exerciseId;
        AppDatabase.DB_EXECUTOR.execute(() -> {
            Exercise ex = exerciseDao.getById(exerciseId);
            ExerciseEntry entry = new ExerciseEntry(exerciseId,
                    ex == null ? "?" : ex.name, 99);
            exercises.postValue(Collections.singletonList(entry));
            currentIdx.postValue(0);
            currentSetNumber.postValue(1);
            refreshSetsForCurrent(Collections.singletonList(entry), 0);
        });
    }

    public boolean logSet(float weightKg, int repsDone) {
        List<ExerciseEntry> list = exercises.getValue();
        Integer idx = currentIdx.getValue();
        Integer setNo = currentSetNumber.getValue();
        if (list == null || list.isEmpty() || idx == null || setNo == null) return false;
        if (idx >= list.size()) return false;
        ExerciseEntry entry = list.get(idx);

        manager.logSet(entry.exerciseId, setNo, weightKg, repsDone);
        currentSetNumber.setValue(setNo + 1);
        refreshSetsForCurrent(list, idx);
        return true;
    }

    public void nextExercise() {
        List<ExerciseEntry> list = exercises.getValue();
        Integer idx = currentIdx.getValue();
        if (list == null || idx == null) return;
        if (idx + 1 >= list.size()) return;
        currentIdx.setValue(idx + 1);
        currentSetNumber.setValue(1);
        cancelRestTimer();
        refreshSetsForCurrent(list, idx + 1);
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

    private void refreshSetsForCurrent(List<ExerciseEntry> list, int idx) {
        if (idx < 0 || idx >= list.size()) {
            setsForCurrent.postValue(Collections.emptyList());
            return;
        }
        int exId = list.get(idx).exerciseId;
        List<SessionSet> filtered = new ArrayList<>();
        for (SessionSet s : manager.getCurrentSets()) {
            if (s.exerciseId == exId) filtered.add(s);
        }
        setsForCurrent.postValue(filtered);
    }
}
