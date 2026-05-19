package com.example.liftlog.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.liftlog.data.model.Exercise;

import java.util.List;

@Dao
public interface ExerciseDao {

    @Insert
    long insert(Exercise exercise);

    @Update
    void update(Exercise exercise);

    @Delete
    void delete(Exercise exercise);

    @Query("SELECT * FROM exercises ORDER BY name ASC")
    LiveData<List<Exercise>> getAllExercises();

    @Query("SELECT * FROM exercises WHERE muscle_group = :group ORDER BY name ASC")
    LiveData<List<Exercise>> getByMuscleGroup(String group);

    @Query("SELECT * FROM exercises WHERE id = :id")
    Exercise getById(int id);

    @Query("SELECT * FROM exercises WHERE is_custom = 1 ORDER BY name ASC")
    LiveData<List<Exercise>> getCustomExercises();

    @Query("SELECT DISTINCT muscle_group FROM exercises ORDER BY muscle_group ASC")
    LiveData<List<String>> getAllMuscleGroups();
}
