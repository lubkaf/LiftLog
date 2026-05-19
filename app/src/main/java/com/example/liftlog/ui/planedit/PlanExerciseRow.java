package com.example.liftlog.ui.planedit;

public class PlanExerciseRow {
    public int id; // 0 = nowy, jeszcze nie zapisany
    public int exerciseId;
    public String exerciseName;
    public int sets;
    public int reps;

    public PlanExerciseRow(int id, int exerciseId, String exerciseName, int sets, int reps) {
        this.id = id;
        this.exerciseId = exerciseId;
        this.exerciseName = exerciseName == null ? "" : exerciseName;
        this.sets = sets;
        this.reps = reps;
    }
}
