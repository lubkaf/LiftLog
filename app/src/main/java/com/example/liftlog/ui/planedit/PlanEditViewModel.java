package com.example.liftlog.ui.planedit;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.liftlog.data.AppDatabase;
import com.example.liftlog.data.dao.ExerciseDao;
import com.example.liftlog.data.dao.PlanExerciseDao;
import com.example.liftlog.data.model.Exercise;
import com.example.liftlog.data.model.PlanExercise;
import com.example.liftlog.data.model.TrainingPlan;
import com.example.liftlog.data.repository.TrainingPlanRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PlanEditViewModel extends AndroidViewModel {

    public static class InitialData {
        public final String name;
        public final List<PlanExerciseRow> rows;

        public InitialData(String name, List<PlanExerciseRow> rows) {
            this.name = name;
            this.rows = rows;
        }
    }

    private final TrainingPlanRepository repository;
    private final ExerciseDao exerciseDao;
    private final PlanExerciseDao planExerciseDao;

    private final MutableLiveData<InitialData> initial = new MutableLiveData<>(null);
    private final MutableLiveData<PlanExerciseRow> exerciseAdded = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> saved = new MutableLiveData<>(false);

    private int planId = -1;
    private String createdAt;
    private boolean loaded = false;

    public PlanEditViewModel(@NonNull Application application) {
        super(application);
        repository = new TrainingPlanRepository(application);
        AppDatabase db = AppDatabase.getInstance(application);
        exerciseDao = db.exerciseDao();
        planExerciseDao = db.planExerciseDao();
    }

    public LiveData<InitialData> getInitial() {
        return initial;
    }

    public LiveData<PlanExerciseRow> getExerciseAdded() {
        return exerciseAdded;
    }

    public LiveData<Boolean> getSaved() {
        return saved;
    }

    public void load(int planId) {
        if (loaded) return;
        loaded = true;
        this.planId = planId;
        if (planId <= 0) {
            initial.postValue(new InitialData("", new ArrayList<>()));
            return;
        }
        AppDatabase.DB_EXECUTOR.execute(() -> {
            TrainingPlan plan = AppDatabase.getInstance(getApplication())
                    .trainingPlanDao().getById(planId);
            if (plan == null) {
                initial.postValue(new InitialData("", new ArrayList<>()));
                return;
            }
            createdAt = plan.createdAt;
            List<PlanExercise> pes = planExerciseDao.getExercisesForPlanSync(planId);
            List<PlanExerciseRow> mapped = new ArrayList<>(pes.size());
            for (PlanExercise pe : pes) {
                Exercise ex = exerciseDao.getById(pe.exerciseId);
                mapped.add(new PlanExerciseRow(pe.id, pe.exerciseId,
                        ex == null ? "?" : ex.name, pe.sets, pe.reps));
            }
            initial.postValue(new InitialData(plan.name, mapped));
        });
    }

    public void requestAddExercise(int exerciseId) {
        AppDatabase.DB_EXECUTOR.execute(() -> {
            Exercise ex = exerciseDao.getById(exerciseId);
            PlanExerciseRow row = new PlanExerciseRow(0, exerciseId,
                    ex == null ? "?" : ex.name, 3, 10);
            exerciseAdded.postValue(row);
        });
    }

    public void consumeExerciseAdded() {
        exerciseAdded.setValue(null);
    }

    public boolean save(String name, List<PlanExerciseRow> rows) {
        if (name == null || name.trim().isEmpty()) return false;
        if (rows == null || rows.isEmpty()) return false;

        TrainingPlan plan = new TrainingPlan();
        plan.id = (planId > 0) ? planId : 0;
        plan.name = name.trim();
        plan.createdAt = (createdAt != null) ? createdAt
                : LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        List<PlanExercise> toSave = new ArrayList<>(rows.size());
        for (int i = 0; i < rows.size(); i++) {
            PlanExerciseRow r = rows.get(i);
            PlanExercise pe = new PlanExercise();
            pe.exerciseId = r.exerciseId;
            pe.sets = r.sets;
            pe.reps = r.reps;
            pe.orderIndex = i;
            toSave.add(pe);
        }

        repository.savePlanWithExercises(plan, toSave);
        saved.postValue(true);
        return true;
    }
}
