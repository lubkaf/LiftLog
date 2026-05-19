package com.example.liftlog.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.liftlog.data.AppDatabase;
import com.example.liftlog.data.dao.ExerciseDao;
import com.example.liftlog.data.model.Exercise;

import java.util.List;

public class ExerciseRepository {

    private final ExerciseDao dao;

    public ExerciseRepository(Context context) {
        dao = AppDatabase.getInstance(context).exerciseDao();
    }

    public void insert(Exercise exercise) {
        AppDatabase.DB_EXECUTOR.execute(() -> dao.insert(exercise));
    }

    public void update(Exercise exercise) {
        AppDatabase.DB_EXECUTOR.execute(() -> dao.update(exercise));
    }

    public void delete(Exercise exercise) {
        AppDatabase.DB_EXECUTOR.execute(() -> dao.delete(exercise));
    }

    public LiveData<List<Exercise>> getAllExercises() {
        return dao.getAllExercises();
    }

    public LiveData<List<Exercise>> getByMuscleGroup(String group) {
        return dao.getByMuscleGroup(group);
    }

    public LiveData<List<Exercise>> getCustomExercises() {
        return dao.getCustomExercises();
    }

    public LiveData<List<String>> getAllMuscleGroups() {
        return dao.getAllMuscleGroups();
    }
}
