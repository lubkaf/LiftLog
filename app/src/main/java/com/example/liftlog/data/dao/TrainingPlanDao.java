package com.example.liftlog.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.liftlog.data.model.TrainingPlan;

import java.util.List;

@Dao
public interface TrainingPlanDao {

    @Insert
    long insert(TrainingPlan plan);

    @Update
    void update(TrainingPlan plan);

    @Delete
    void delete(TrainingPlan plan);

    @Query("SELECT * FROM training_plans ORDER BY created_at DESC")
    LiveData<List<TrainingPlan>> getAllPlans();

    @Query("SELECT * FROM training_plans WHERE id = :id")
    TrainingPlan getById(int id);
}
