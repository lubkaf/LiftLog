package com.example.liftlog.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.liftlog.data.model.WorkoutSession;

import java.util.List;

@Dao
public interface WorkoutSessionDao {

    @Insert
    long insert(WorkoutSession session);

    @Update
    void update(WorkoutSession session);

    @Delete
    void delete(WorkoutSession session);

    @Query("SELECT * FROM workout_sessions ORDER BY date DESC")
    LiveData<List<WorkoutSession>> getAllSessions();

    @Query("SELECT * FROM workout_sessions WHERE id = :id")
    WorkoutSession getById(int id);

    @Query("SELECT * FROM workout_sessions ORDER BY date DESC LIMIT 1")
    WorkoutSession getLastSession();

    @Query("SELECT * FROM workout_sessions WHERE date BETWEEN :from AND :to ORDER BY date DESC")
    LiveData<List<WorkoutSession>> getSessionsInRange(String from, String to);

    @Query("SELECT COUNT(*) FROM workout_sessions")
    int getTotalSessionCount();
}
