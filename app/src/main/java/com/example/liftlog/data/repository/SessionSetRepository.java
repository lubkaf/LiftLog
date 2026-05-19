package com.example.liftlog.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.liftlog.data.AppDatabase;
import com.example.liftlog.data.dao.SessionSetDao;
import com.example.liftlog.data.model.SessionSet;
import com.example.liftlog.data.model.WeightDateTuple;

import java.util.List;

public class SessionSetRepository {

    private final SessionSetDao dao;

    public SessionSetRepository(Context context) {
        dao = AppDatabase.getInstance(context).sessionSetDao();
    }

    public void insert(SessionSet set) {
        AppDatabase.DB_EXECUTOR.execute(() -> dao.insert(set));
    }

    public void update(SessionSet set) {
        AppDatabase.DB_EXECUTOR.execute(() -> dao.update(set));
    }

    public void delete(SessionSet set) {
        AppDatabase.DB_EXECUTOR.execute(() -> dao.delete(set));
    }

    public LiveData<List<SessionSet>> getSetsForSession(int sessionId) {
        return dao.getSetsForSession(sessionId);
    }

    public LiveData<List<WeightDateTuple>> getWeightProgressForExercise(int exerciseId) {
        return dao.getWeightProgressForExercise(exerciseId);
    }

    public float getPersonalRecord(int exerciseId) {
        Float result = dao.getPersonalRecord(exerciseId);
        return result != null ? result : 0f;
    }

    public float getTotalVolumeForSession(int sessionId) {
        Float result = dao.getTotalVolumeForSession(sessionId);
        return result != null ? result : 0f;
    }
}
