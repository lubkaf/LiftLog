package com.example.liftlog.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "plan_exercises",
    foreignKeys = {
        @ForeignKey(
            entity = TrainingPlan.class,
            parentColumns = "id",
            childColumns = "plan_id",
            onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
            entity = Exercise.class,
            parentColumns = "id",
            childColumns = "exercise_id",
            onDelete = ForeignKey.CASCADE
        )
    },
    indices = {
        @Index("plan_id"),
        @Index("exercise_id")
    }
)
public class PlanExercise {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "plan_id")
    public int planId;

    @ColumnInfo(name = "exercise_id")
    public int exerciseId;

    public int sets;
    public int reps;

    @ColumnInfo(name = "order_index")
    public int orderIndex;
}
