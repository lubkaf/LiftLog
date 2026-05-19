package com.example.liftlog.ui.dashboard;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.liftlog.data.AppDatabase;
import com.example.liftlog.data.model.WorkoutSession;
import com.example.liftlog.data.repository.WorkoutSessionRepository;

public class DashboardViewModel extends AndroidViewModel {

    public static class LastWorkout {
        public final String dateIso;
        public final int durationMinutes;
        public final float volumeKg;

        public LastWorkout(String dateIso, int durationMinutes, float volumeKg) {
            this.dateIso = dateIso;
            this.durationMinutes = durationMinutes;
            this.volumeKg = volumeKg;
        }
    }

    private final WorkoutSessionRepository repository;
    private final MutableLiveData<LastWorkout> lastWorkout = new MutableLiveData<>(null);

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        repository = new WorkoutSessionRepository(application);
    }

    public LiveData<LastWorkout> getLastWorkout() {
        return lastWorkout;
    }

    public void refresh() {
        AppDatabase.DB_EXECUTOR.execute(() -> {
            WorkoutSession last = repository.getLastSessionSync();
            if (last == null) {
                lastWorkout.postValue(null);
                return;
            }
            float volume = repository.getTotalVolumeForSession(last.id);
            lastWorkout.postValue(new LastWorkout(last.date, last.durationMinutes, volume));
        });
    }
}
