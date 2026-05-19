package com.example.liftlog.ui.history;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.liftlog.data.AppDatabase;
import com.example.liftlog.data.dao.ExerciseDao;
import com.example.liftlog.data.dao.SessionSetDao;
import com.example.liftlog.data.model.Exercise;
import com.example.liftlog.data.model.SessionSet;
import com.example.liftlog.data.model.WeightDateTuple;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SessionDetailViewModel extends AndroidViewModel {

    public static class ExerciseRef {
        public final int id;
        public final String name;

        public ExerciseRef(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    private final SessionSetDao setDao;
    private final ExerciseDao exerciseDao;

    private final MutableLiveData<List<SessionSet>> sets = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<ExerciseRef>> exercisesInSession = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Integer> selectedExerciseId = new MutableLiveData<>(null);

    private int sessionId = -1;

    public SessionDetailViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        setDao = db.sessionSetDao();
        exerciseDao = db.exerciseDao();
    }

    public LiveData<List<SessionSet>> getSets() { return sets; }
    public LiveData<List<ExerciseRef>> getExercisesInSession() { return exercisesInSession; }
    public LiveData<Integer> getSelectedExerciseId() { return selectedExerciseId; }

    public LiveData<List<WeightDateTuple>> getProgressFor(int exerciseId) {
        return setDao.getWeightProgressForExercise(exerciseId);
    }

    public void load(int sessionId) {
        if (this.sessionId == sessionId) return;
        this.sessionId = sessionId;
        AppDatabase.DB_EXECUTOR.execute(() -> {
            List<SessionSet> sessionSets = setDao.getSetsForSessionSync(sessionId);
            sets.postValue(sessionSets);

            // Unikalne exercise_id w sesji, kolejność wystąpień
            Map<Integer, String> seen = new LinkedHashMap<>();
            for (SessionSet s : sessionSets) {
                if (!seen.containsKey(s.exerciseId)) {
                    Exercise ex = exerciseDao.getById(s.exerciseId);
                    seen.put(s.exerciseId, ex == null ? "?" : ex.name);
                }
            }
            List<ExerciseRef> refs = new ArrayList<>();
            for (Map.Entry<Integer, String> e : seen.entrySet()) {
                refs.add(new ExerciseRef(e.getKey(), e.getValue()));
            }
            exercisesInSession.postValue(refs);
            if (!refs.isEmpty()) {
                selectedExerciseId.postValue(refs.get(0).id);
            }
        });
    }

    public void selectExercise(int exerciseId) {
        selectedExerciseId.setValue(exerciseId);
    }
}
