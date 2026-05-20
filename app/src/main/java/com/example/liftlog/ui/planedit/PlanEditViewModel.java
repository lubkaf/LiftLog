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
import java.util.Collections;
import java.util.List;

public class PlanEditViewModel extends AndroidViewModel {

    private final TrainingPlanRepository repository;
    private final ExerciseDao exerciseDao;
    private final PlanExerciseDao planExerciseDao;

    // Kanonalna lista ćwiczeń — obiekty współdzielone z adapterem (TextWatcher mutuje je bezpośrednio)
    private final List<PlanExerciseRow> rows = new ArrayList<>();

    private final MutableLiveData<String> planName = new MutableLiveData<>(null);
    private final MutableLiveData<List<PlanExerciseRow>> currentRows = new MutableLiveData<>(new ArrayList<>());
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

    public LiveData<String> getPlanName() { return planName; }
    public LiveData<List<PlanExerciseRow>> getCurrentRows() { return currentRows; }
    public LiveData<Boolean> getSaved() { return saved; }

    public void load(int planId) {
        if (loaded) return;
        loaded = true;
        this.planId = planId;
        if (planId <= 0) {
            planName.postValue("");
            currentRows.postValue(new ArrayList<>());
            return;
        }
        AppDatabase.DB_EXECUTOR.execute(() -> {
            TrainingPlan plan = AppDatabase.getInstance(getApplication())
                    .trainingPlanDao().getById(planId);
            if (plan == null) {
                planName.postValue("");
                currentRows.postValue(new ArrayList<>());
                return;
            }
            createdAt = plan.createdAt;
            List<PlanExercise> pes = planExerciseDao.getExercisesForPlanSync(planId);
            rows.clear();
            for (PlanExercise pe : pes) {
                Exercise ex = exerciseDao.getById(pe.exerciseId);
                rows.add(new PlanExerciseRow(pe.id, pe.exerciseId,
                        ex == null ? "?" : ex.name, pe.sets, pe.reps));
            }
            planName.postValue(plan.name);
            currentRows.postValue(new ArrayList<>(rows));
        });
    }

    public void requestAddExercise(int exerciseId) {
        AppDatabase.DB_EXECUTOR.execute(() -> {
            Exercise ex = exerciseDao.getById(exerciseId);
            PlanExerciseRow row = new PlanExerciseRow(0, exerciseId,
                    ex == null ? "?" : ex.name, 3, 10);
            rows.add(row);
            currentRows.postValue(new ArrayList<>(rows));
        });
    }

    // Wywoływać z wątku UI, po tym jak adapter już usunął element wizualnie
    public void removeExercise(int position) {
        if (position < 0 || position >= rows.size()) return;
        rows.remove(position);
    }

    // Wywoływać z wątku UI, po tym jak adapter już przesunął element wizualnie
    public void moveExercise(int from, int to) {
        if (from < 0 || to < 0 || from >= rows.size() || to >= rows.size()) return;
        Collections.swap(rows, from, to);
    }

    public boolean save(String name) {
        if (name == null || name.trim().isEmpty()) return false;
        if (rows.isEmpty()) return false;

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
