package com.example.liftlog.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.liftlog.data.AppDatabase;
import com.example.liftlog.data.dao.PlanExerciseDao;
import com.example.liftlog.data.dao.TrainingPlanDao;
import com.example.liftlog.data.model.PlanExercise;
import com.example.liftlog.data.model.TrainingPlan;

import java.util.ArrayList;
import java.util.List;

public class TrainingPlanRepository {

    private final TrainingPlanDao planDao;
    private final PlanExerciseDao planExerciseDao;
    private final AppDatabase db;

    public TrainingPlanRepository(Context context) {
        db = AppDatabase.getInstance(context);
        planDao = db.trainingPlanDao();
        planExerciseDao = db.planExerciseDao();
    }

    public void insert(TrainingPlan plan) {
        AppDatabase.DB_EXECUTOR.execute(() -> planDao.insert(plan));
    }

    public void update(TrainingPlan plan) {
        AppDatabase.DB_EXECUTOR.execute(() -> planDao.update(plan));
    }

    public void delete(TrainingPlan plan) {
        AppDatabase.DB_EXECUTOR.execute(() -> planDao.delete(plan));
    }

    public LiveData<List<TrainingPlan>> getAllPlans() {
        return planDao.getAllPlans();
    }

    // Atomowa aktualizacja planu + jego ćwiczeń
    public void savePlanWithExercises(TrainingPlan plan, List<PlanExercise> exercises) {
        AppDatabase.DB_EXECUTOR.execute(() -> db.runInTransaction(() -> {
            long planId;
            if (plan.id == 0) {
                planId = planDao.insert(plan);
            } else {
                planDao.update(plan);
                planId = plan.id;
            }
            planExerciseDao.deleteAllForPlan((int) planId);
            List<PlanExercise> copies = new ArrayList<>(exercises.size());
            for (PlanExercise pe : exercises) {
                PlanExercise copy = new PlanExercise();
                copy.exerciseId = pe.exerciseId;
                copy.sets = pe.sets;
                copy.reps = pe.reps;
                copy.orderIndex = pe.orderIndex;
                copy.planId = (int) planId;
                copies.add(copy);
            }
            planExerciseDao.insertAll(copies);
        }));
    }

    public LiveData<List<PlanExercise>> getExercisesForPlan(int planId) {
        return planExerciseDao.getExercisesForPlan(planId);
    }
}
