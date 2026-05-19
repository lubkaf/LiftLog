package com.example.liftlog.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.liftlog.data.dao.ExerciseDao;
import com.example.liftlog.data.dao.PlanExerciseDao;
import com.example.liftlog.data.dao.SessionSetDao;
import com.example.liftlog.data.dao.TrainingPlanDao;
import com.example.liftlog.data.dao.WorkoutSessionDao;
import com.example.liftlog.data.model.Exercise;
import com.example.liftlog.data.model.PlanExercise;
import com.example.liftlog.data.model.SessionSet;
import com.example.liftlog.data.model.TrainingPlan;
import com.example.liftlog.data.model.WorkoutSession;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(
    entities = {
        Exercise.class,
        TrainingPlan.class,
        PlanExercise.class,
        WorkoutSession.class,
        SessionSet.class
    },
    version = 1,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;
    public static final ExecutorService DB_EXECUTOR = Executors.newSingleThreadExecutor();

    public abstract ExerciseDao exerciseDao();
    public abstract TrainingPlanDao trainingPlanDao();
    public abstract PlanExerciseDao planExerciseDao();
    public abstract WorkoutSessionDao workoutSessionDao();
    public abstract SessionSetDao sessionSetDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "liftlog_db"
                        )
                        .addCallback(PREPOPULATE_CALLBACK)
                        .build();
                }
            }
        }
        return INSTANCE;
    }

    private static final RoomDatabase.Callback PREPOPULATE_CALLBACK = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            DB_EXECUTOR.execute(() -> {
                AppDatabase database = INSTANCE;
                if (database != null) {
                    DatabaseSeeder.seed(database.exerciseDao());
                }
            });
        }
    };
}
