package com.example.liftlog.ui.library;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.liftlog.data.model.Exercise;
import com.example.liftlog.data.repository.ExerciseRepository;

import java.util.ArrayList;
import java.util.List;

public class LibraryViewModel extends AndroidViewModel {

    public static final String GROUP_ALL = "__ALL__";

    private final ExerciseRepository repository;
    private final MutableLiveData<String> selectedGroup = new MutableLiveData<>(GROUP_ALL);
    private final MediatorLiveData<List<Exercise>> filtered = new MediatorLiveData<>();
    private List<Exercise> latestAll = new ArrayList<>();

    public LibraryViewModel(@NonNull Application application) {
        super(application);
        repository = new ExerciseRepository(application);
        filtered.addSource(repository.getAllExercises(), list -> {
            latestAll = list == null ? new ArrayList<>() : list;
            applyFilter();
        });
        filtered.addSource(selectedGroup, group -> applyFilter());
    }

    public LiveData<List<Exercise>> getFilteredExercises() {
        return filtered;
    }

    public LiveData<List<String>> getMuscleGroups() {
        return repository.getAllMuscleGroups();
    }

    public void selectGroup(String group) {
        if (group == null) group = GROUP_ALL;
        if (group.equals(selectedGroup.getValue())) return;
        selectedGroup.setValue(group);
    }

    public void addCustom(String name, String muscleGroup, String description) {
        if (name == null || name.trim().isEmpty()) return;
        Exercise ex = new Exercise();
        ex.name = name.trim();
        ex.muscleGroup = (muscleGroup == null || muscleGroup.trim().isEmpty())
                ? "Inne" : muscleGroup.trim();
        ex.description = description == null ? null : description.trim();
        ex.isCustom = 1;
        repository.insert(ex);
    }

    /**
     * TODO(bogdans): wstrzyknąć {@link com.example.liftlog.data.remote.ExerciseApiService}
     * i pobrać listę ćwiczeń z ExerciseDB (RapidAPI). Po zwróceniu:
     *   - mapować na Exercise (isCustom=0)
     *   - insert do bazy przez repository (deduplikacja po name)
     * Na razie no-op — przycisk w UI jest wyłączony.
     */
    public void fetchFromExerciseDb() {
        // Pozostawione celowo puste do czasu zrobienia API przez bogdans.
    }

    private void applyFilter() {
        String group = selectedGroup.getValue();
        if (group == null || GROUP_ALL.equals(group)) {
            filtered.setValue(new ArrayList<>(latestAll));
            return;
        }
        List<Exercise> result = new ArrayList<>();
        for (Exercise e : latestAll) {
            if (group.equals(e.muscleGroup)) result.add(e);
        }
        filtered.setValue(result);
    }
}
