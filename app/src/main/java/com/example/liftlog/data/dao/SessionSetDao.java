package com.example.liftlog.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.liftlog.data.model.SessionSet;
import com.example.liftlog.data.model.WeightDateTuple;

import java.util.List;

@Dao
public interface SessionSetDao {

    @Insert
    void insert(SessionSet set);

    @Insert
    void insertAll(List<SessionSet> sets);

    @Update
    void update(SessionSet set);

    @Delete
    void delete(SessionSet set);

    @Query("SELECT * FROM session_sets WHERE session_id = :sessionId ORDER BY exercise_id, set_number ASC")
    LiveData<List<SessionSet>> getSetsForSession(int sessionId);

    @Query("SELECT * FROM session_sets WHERE session_id = :sessionId ORDER BY exercise_id, set_number ASC")
    List<SessionSet> getSetsForSessionSync(int sessionId);

    @Query("SELECT MAX(weight_kg) FROM session_sets WHERE exercise_id = :exerciseId")
    float getPersonalRecord(int exerciseId);

    @Query("SELECT SUM(weight_kg * reps_done) FROM session_sets WHERE session_id = :sessionId")
    float getTotalVolumeForSession(int sessionId);

    @Query("SELECT MAX(weight_kg) FROM session_sets WHERE exercise_id = :exerciseId AND session_id = :sessionId")
    float getMaxWeightInSession(int exerciseId, int sessionId);

    @Query("SELECT ss.weight_kg, ws.date FROM session_sets ss " +
           "JOIN workout_sessions ws ON ss.session_id = ws.id " +
           "WHERE ss.exercise_id = :exerciseId ORDER BY ws.date ASC")
    LiveData<List<WeightDateTuple>> getWeightProgressForExercise(int exerciseId);
}
