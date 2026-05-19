package com.example.liftlog.data.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "exercises")
public class Exercise {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    public String name = "";

    @ColumnInfo(name = "muscle_group")
    public String muscleGroup;

    @ColumnInfo(name = "is_custom")
    public int isCustom; // 0 = biblioteczne, 1 = własne

    public String description;
}
