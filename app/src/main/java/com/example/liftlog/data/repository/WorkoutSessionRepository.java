package com.example.liftlog.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.liftlog.data.AppDatabase;
import com.example.liftlog.data.dao.SessionSetDao;
import com.example.liftlog.data.dao.WorkoutSessionDao;
import com.example.liftlog.data.model.SessionSet;
import com.example.liftlog.data.model.WorkoutSession;

import java.util.List;

public class WorkoutSessionRepository {

    private final WorkoutSessionDao sessionDao;
    private final SessionSetDao setDao;
    private final AppDatabase db;

    public WorkoutSessionRepository(Context context) {
        db = AppDatabase.getInstance(context);
        sessionDao = db.workoutSessionDao();
        setDao = db.sessionSetDao();
    }

    public LiveData<List<WorkoutSession>> getAllSessions() {
        return sessionDao.getAllSessions();
    }

    public LiveData<List<WorkoutSession>> getSessionsInRange(String from, String to) {
        return sessionDao.getSessionsInRange(from, to);
    }

    // Atomowy zapis zakończonej sesji wraz ze wszystkimi seriami
    public void saveCompletedSession(WorkoutSession session, List<SessionSet> sets) {
        AppDatabase.DB_EXECUTOR.execute(() -> db.runInTransaction(() -> {
            long sessionId = sessionDao.insert(session);
            for (SessionSet s : sets) {
                s.sessionId = (int) sessionId;
            }
            setDao.insertAll(sets);
        }));
    }

    public void delete(WorkoutSession session) {
        AppDatabase.DB_EXECUTOR.execute(() -> sessionDao.delete(session));
    }

    // Synchroniczny odczyt do Dashboard (ostatni trening)
    public WorkoutSession getLastSessionSync() {
        // Wywoływać tylko z wątku tła
        return sessionDao.getLastSession();
    }

    public float getTotalVolumeForSession(int sessionId) {
        // Wywoływać tylko z wątku tła
        return setDao.getTotalVolumeForSession(sessionId);
    }

    public float getPersonalRecord(int exerciseId) {
        // Wywoływać tylko z wątku tła
        return setDao.getPersonalRecord(exerciseId);
    }

    public LiveData<List<SessionSet>> getSetsForSession(int sessionId) {
        return setDao.getSetsForSession(sessionId);
    }
}
