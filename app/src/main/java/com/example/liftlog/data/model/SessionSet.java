package com.example.liftlog.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "session_sets",
    foreignKeys = {
        @ForeignKey(
            entity = WorkoutSession.class,
            parentColumns = "id",
            childColumns = "session_id",
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
        @Index("session_id"),
        @Index("exercise_id")
    }
)
public class SessionSet {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "session_id")
    public int sessionId;

    @ColumnInfo(name = "exercise_id")
    public int exerciseId;

    @ColumnInfo(name = "set_number")
    public int setNumber;

    @ColumnInfo(name = "weight_kg")
    public float weightKg;

    @ColumnInfo(name = "reps_done")
    public int repsDone;
}
