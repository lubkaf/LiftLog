package com.example.liftlog.data.model;

import androidx.room.ColumnInfo;

public class WeightDateTuple {
    @ColumnInfo(name = "weight_kg")
    public float weightKg;
    public String date;
}
