package com.example.liftlog.ui.summary;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.liftlog.data.AppDatabase;
import com.example.liftlog.data.dao.ExerciseDao;
import com.example.liftlog.data.dao.SessionSetDao;
import com.example.liftlog.data.dao.WorkoutSessionDao;
import com.example.liftlog.data.model.Exercise;
import com.example.liftlog.data.model.SessionSet;
import com.example.liftlog.data.model.WorkoutSession;
import com.example.liftlog.logic.WorkoutStatsCalculator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SummaryViewModel extends AndroidViewModel {

    public static class PrItem {
        public final String exerciseName;
        public final float weight;

        public PrItem(String exerciseName, float weight) {
            this.exerciseName = exerciseName;
            this.weight = weight;
        }
    }

    public static class SummaryState {
        public final boolean hasSession;
        public final float volume;
        public final int durationMinutes;
        public final int setsCount;
        public final List<PrItem> personalRecords;

        public SummaryState(boolean hasSession, float volume, int durationMinutes,
                            int setsCount, List<PrItem> personalRecords) {
            this.hasSession = hasSession;
            this.volume = volume;
            this.durationMinutes = durationMinutes;
            this.setsCount = setsCount;
            this.personalRecords = personalRecords;
        }
    }

    private final MutableLiveData<SummaryState> state = new MutableLiveData<>(null);
    private final WorkoutSessionDao sessionDao;
    private final SessionSetDao setDao;
    private final ExerciseDao exerciseDao;

    private boolean loaded = false;

    public SummaryViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        sessionDao = db.workoutSessionDao();
        setDao = db.sessionSetDao();
        exerciseDao = db.exerciseDao();
    }

    public LiveData<SummaryState> getState() {
        return state;
    }

    public void loadLatest() {
        if (loaded) return;
        loaded = true;
        AppDatabase.DB_EXECUTOR.execute(() -> {
            WorkoutSession last = sessionDao.getLastSession();
            if (last == null) {
                state.postValue(new SummaryState(false, 0f, 0, 0, new ArrayList<>()));
                return;
            }
            List<SessionSet> sets = setDao.getSetsForSessionSync(last.id);
            float volume = WorkoutStatsCalculator.calculateVolume(sets);

            // Per exercise: max waga w tej sesji
            Map<Integer, Float> maxPerExercise = new HashMap<>();
            for (SessionSet s : sets) {
                Float cur = maxPerExercise.get(s.exerciseId);
                if (cur == null || s.weightKg > cur) {
                    maxPerExercise.put(s.exerciseId, s.weightKg);
                }
            }

            List<PrItem> prs = new ArrayList<>();
            for (Map.Entry<Integer, Float> e : maxPerExercise.entrySet()) {
                float sessionMax = e.getValue();
                if (sessionMax <= 0f) continue;
                Float globalMaxBoxed = setDao.getPersonalRecord(e.getKey());
                float globalMax = globalMaxBoxed != null ? globalMaxBoxed : 0f;
                // Backend liczy globalny max łącznie z bieżącą sesją (= sessionMax po zapisie).
                // Jeśli equals — to znaczy, że obecna sesja ustanowiła lub wyrównała rekord.
                if (sessionMax >= globalMax) {
                    Exercise ex = exerciseDao.getById(e.getKey());
                    prs.add(new PrItem(ex == null ? "?" : ex.name, sessionMax));
                }
            }
            state.postValue(new SummaryState(true, volume, last.durationMinutes, sets.size(), prs));
        });
    }
}
