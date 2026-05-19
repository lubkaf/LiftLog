package com.example.liftlog.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.liftlog.data.model.PlanExercise;

import java.util.List;

@Dao
public interface PlanExerciseDao {

    @Insert
    void insert(PlanExercise planExercise);

    @Insert
    void insertAll(List<PlanExercise> planExercises);

    @Update
    void update(PlanExercise planExercise);

    @Delete
    void delete(PlanExercise planExercise);

    @Query("SELECT * FROM plan_exercises WHERE plan_id = :planId ORDER BY order_index ASC")
    LiveData<List<PlanExercise>> getExercisesForPlan(int planId);

    @Query("SELECT * FROM plan_exercises WHERE plan_id = :planId ORDER BY order_index ASC")
    List<PlanExercise> getExercisesForPlanSync(int planId);

    @Query("DELETE FROM plan_exercises WHERE plan_id = :planId")
    void deleteAllForPlan(int planId);
}
