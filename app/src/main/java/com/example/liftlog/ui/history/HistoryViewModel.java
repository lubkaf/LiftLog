package com.example.liftlog.ui.history;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.liftlog.data.AppDatabase;
import com.example.liftlog.data.model.WorkoutSession;
import com.example.liftlog.data.repository.WorkoutSessionRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class HistoryViewModel extends AndroidViewModel {

    private final WorkoutSessionRepository repository;
    private final MediatorLiveData<List<HistoryItem>> items = new MediatorLiveData<>();
    private final AtomicInteger generation = new AtomicInteger(0);

    public HistoryViewModel(@NonNull Application application) {
        super(application);
        repository = new WorkoutSessionRepository(application);
        LiveData<List<WorkoutSession>> source = repository.getAllSessions();
        items.addSource(source, this::recompute);
    }

    public LiveData<List<HistoryItem>> getItems() {
        return items;
    }

    private void recompute(List<WorkoutSession> sessions) {
        int gen = generation.incrementAndGet();
        if (sessions == null || sessions.isEmpty()) {
            items.setValue(new ArrayList<>());
            return;
        }
        List<WorkoutSession> snapshot = new ArrayList<>(sessions);
        AppDatabase.DB_EXECUTOR.execute(() -> {
            List<HistoryItem> result = new ArrayList<>(snapshot.size());
            for (WorkoutSession s : snapshot) {
                float vol = repository.getTotalVolumeForSession(s.id);
                result.add(new HistoryItem(s.id, s.date, s.durationMinutes, vol));
            }
            if (gen == generation.get()) {
                items.postValue(result);
            }
        });
    }
}
