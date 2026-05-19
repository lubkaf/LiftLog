package com.example.liftlog.ui.plans;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.liftlog.data.model.TrainingPlan;
import com.example.liftlog.data.repository.TrainingPlanRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PlansViewModel extends AndroidViewModel {

    private final TrainingPlanRepository repository;

    public PlansViewModel(@NonNull Application application) {
        super(application);
        repository = new TrainingPlanRepository(application);
    }

    public LiveData<List<TrainingPlan>> getPlans() {
        return repository.getAllPlans();
    }

    public void createPlan(String name) {
        TrainingPlan plan = new TrainingPlan();
        plan.name = name;
        plan.createdAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        repository.insert(plan);
    }

    public void deletePlan(TrainingPlan plan) {
        repository.delete(plan);
    }
}
