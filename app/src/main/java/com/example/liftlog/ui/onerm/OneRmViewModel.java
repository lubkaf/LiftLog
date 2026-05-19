package com.example.liftlog.ui.onerm;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class OneRmViewModel extends ViewModel {

    private final MutableLiveData<Float> result = new MutableLiveData<>(null);

    public LiveData<Float> getResult() {
        return result;
    }

    public boolean compute(OneRmCalculator.Formula formula, Float weight, Integer reps) {
        if (weight == null || reps == null) return false;
        if (weight <= 0f) return false;
        if (reps < 1 || reps > 20) return false;
        result.setValue(OneRmCalculator.compute(formula, weight, reps));
        return true;
    }
}
