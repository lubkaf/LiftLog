package com.example.liftlog.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.liftlog.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DashboardFragment extends Fragment {

    private DashboardViewModel viewModel;
    private TextView textLastWorkout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        textLastWorkout = view.findViewById(R.id.text_last_workout);
        MaterialButton btnPlans = view.findViewById(R.id.btn_plans);
        MaterialButton btnHistory = view.findViewById(R.id.btn_history);
        MaterialButton btnLibrary = view.findViewById(R.id.btn_library);
        MaterialButton btnOneRm = view.findViewById(R.id.btn_one_rm);
        ExtendedFloatingActionButton fab = view.findViewById(R.id.fab_quick_start);

        btnPlans.setOnClickListener(v -> navigate(R.id.action_dashboard_to_plans));
        btnHistory.setOnClickListener(v -> navigate(R.id.action_dashboard_to_history));
        btnLibrary.setOnClickListener(v -> navigate(R.id.action_dashboard_to_library));
        btnOneRm.setOnClickListener(v -> navigate(R.id.action_dashboard_to_oneRm));
        fab.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putInt("planId", -1);
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_dashboard_to_activeWorkout, args);
        });

        viewModel.getLastWorkout().observe(getViewLifecycleOwner(), this::renderLastWorkout);
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.refresh();
    }

    private void renderLastWorkout(@Nullable DashboardViewModel.LastWorkout last) {
        if (last == null) {
            textLastWorkout.setText(R.string.dashboard_no_workouts_yet);
            return;
        }
        String date = formatDate(last.dateIso);
        textLastWorkout.setText(getString(
                R.string.dashboard_last_workout_summary, date, last.volumeKg, last.durationMinutes));
    }

    private String formatDate(String iso) {
        try {
            LocalDateTime dt = LocalDateTime.parse(iso);
            return dt.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        } catch (DateTimeParseException e) {
            return iso;
        }
    }

    private void navigate(int actionId) {
        NavHostFragment.findNavController(this).navigate(actionId);
    }
}
