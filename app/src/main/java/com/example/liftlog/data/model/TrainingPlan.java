package com.example.liftlog.data.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "training_plans")
public class TrainingPlan {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    public String name = "";

    @ColumnInfo(name = "created_at")
    public String createdAt; // ISO 8601: "2026-04-01T10:00:00"
}
