package com.example.liftlog.data.model;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "workout_sessions",
    foreignKeys = @ForeignKey(
        entity = TrainingPlan.class,
        parentColumns = "id",
        childColumns = "plan_id",
        onDelete = ForeignKey.SET_NULL
    ),
    indices = @Index("plan_id")
)
public class WorkoutSession {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @Nullable
    @ColumnInfo(name = "plan_id")
    public Integer planId; // NULL = trening bez planu

    public String date; // ISO 8601

    @ColumnInfo(name = "duration_minutes")
    public int durationMinutes;

    public String notes;
}
