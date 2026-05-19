package com.example.liftlog.ui.workout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.liftlog.R;
import com.example.liftlog.data.model.Exercise;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class ActiveWorkoutFragment extends Fragment {

    private ActiveWorkoutViewModel viewModel;
    private ActiveSetAdapter setsAdapter;

    private Spinner spinnerExercise;
    private TextView textExerciseName;
    private TextView textSetProgress;
    private TextView textRestRemaining;
    private TextInputEditText editWeight;
    private TextInputEditText editReps;

    private List<Exercise> pickerExercises = new ArrayList<>();
    private ArrayAdapter<String> spinnerAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_active_workout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ActiveWorkoutViewModel.class);
        int planId = getArguments() != null ? getArguments().getInt("planId", -1) : -1;

        spinnerExercise = view.findViewById(R.id.spinner_exercise);
        textExerciseName = view.findViewById(R.id.text_exercise_name);
        textSetProgress = view.findViewById(R.id.text_set_progress);
        textRestRemaining = view.findViewById(R.id.text_rest_remaining);
        editWeight = view.findViewById(R.id.edit_weight);
        editReps = view.findViewById(R.id.edit_reps);
        RecyclerView recycler = view.findViewById(R.id.recycler_sets);
        MaterialButton btnSave = view.findViewById(R.id.btn_save_set);
        MaterialButton btnNext = view.findViewById(R.id.btn_next_exercise);
        MaterialButton btnFinish = view.findViewById(R.id.btn_finish_workout);
        MaterialButton btn60 = view.findViewById(R.id.btn_rest_60);
        MaterialButton btn90 = view.findViewById(R.id.btn_rest_90);
        MaterialButton btn120 = view.findViewById(R.id.btn_rest_120);

        setsAdapter = new ActiveSetAdapter();
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.setAdapter(setsAdapter);

        spinnerAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, new ArrayList<>());
        spinnerExercise.setAdapter(spinnerAdapter);
        spinnerExercise.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                if (position < 0 || position >= pickerExercises.size()) return;
                viewModel.setPickedExercise(pickerExercises.get(position).id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        btnSave.setOnClickListener(v -> onSaveSet());
        btnNext.setOnClickListener(v -> {
            viewModel.nextExercise();
            clearInputs();
        });
        btnFinish.setOnClickListener(v -> showFinishDialog());
        btn60.setOnClickListener(v -> viewModel.startRestTimer(60));
        btn90.setOnClickListener(v -> viewModel.startRestTimer(90));
        btn120.setOnClickListener(v -> viewModel.startRestTimer(120));

        viewModel.initialize(planId);

        viewModel.getExercises().observe(getViewLifecycleOwner(), list -> renderExerciseHeader());
        viewModel.getCurrentIdx().observe(getViewLifecycleOwner(), idx -> renderExerciseHeader());
        viewModel.getCurrentSetNumber().observe(getViewLifecycleOwner(), n -> renderExerciseHeader());
        viewModel.getSetsForCurrent().observe(getViewLifecycleOwner(), setsAdapter::submitList);
        viewModel.getRestRemaining().observe(getViewLifecycleOwner(), this::renderRest);
        viewModel.getFinished().observe(getViewLifecycleOwner(), finished -> {
            if (Boolean.TRUE.equals(finished)) {
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_activeWorkout_to_summary);
            }
        });

        if (viewModel.isQuickStart()) {
            spinnerExercise.setVisibility(View.VISIBLE);
            viewModel.getAllExercisesForPicker().observe(getViewLifecycleOwner(), exercises -> {
                pickerExercises = exercises == null ? new ArrayList<>() : exercises;
                List<String> labels = new ArrayList<>(pickerExercises.size());
                for (Exercise e : pickerExercises) labels.add(e.name);
                spinnerAdapter.clear();
                spinnerAdapter.addAll(labels);
                spinnerAdapter.notifyDataSetChanged();
            });
        }
    }

    private void onSaveSet() {
        String wStr = editWeight.getText() == null ? "" : editWeight.getText().toString();
        String rStr = editReps.getText() == null ? "" : editReps.getText().toString();
        Float weight = parseFloatSafe(wStr);
        Integer reps = parseIntSafe(rStr);
        if (weight == null || reps == null || weight < 0 || reps <= 0) {
            Toast.makeText(requireContext(), R.string.workout_validation_error, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!viewModel.logSet(weight, reps)) {
            Toast.makeText(requireContext(), R.string.workout_no_exercises, Toast.LENGTH_SHORT).show();
            return;
        }
        clearInputs();
    }

    private void clearInputs() {
        editWeight.setText("");
        editReps.setText("");
    }

    private void renderExerciseHeader() {
        List<ActiveWorkoutViewModel.ExerciseEntry> list = viewModel.getExercises().getValue();
        Integer idx = viewModel.getCurrentIdx().getValue();
        Integer setNo = viewModel.getCurrentSetNumber().getValue();
        if (list == null || idx == null || setNo == null) return;
        if (list.isEmpty()) {
            if (viewModel.isQuickStart()) {
                textExerciseName.setText(R.string.workout_select_exercise_hint);
                textSetProgress.setText("");
            } else {
                textExerciseName.setText(R.string.workout_no_exercises);
                textSetProgress.setText("");
            }
            return;
        }
        if (idx >= list.size()) idx = list.size() - 1;
        ActiveWorkoutViewModel.ExerciseEntry entry = list.get(idx);
        textExerciseName.setText(entry.name);
        textSetProgress.setText(getString(R.string.workout_set_x_of_y, setNo, entry.setsTarget));
    }

    private void renderRest(int seconds) {
        textRestRemaining.setText(getString(R.string.workout_rest_format, seconds));
    }

    private void showFinishDialog() {
        EditText input = new EditText(requireContext());
        input.setHint(R.string.workout_finish_dialog_notes);
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.workout_finish_dialog_title)
                .setView(input)
                .setPositiveButton(R.string.common_save, (d, w) -> {
                    String notes = input.getText().toString().trim();
                    viewModel.finishWorkout(notes.isEmpty() ? null : notes);
                })
                .setNegativeButton(R.string.common_cancel, null)
                .show();
    }

    @Nullable
    private static Float parseFloatSafe(String s) {
        if (s == null || s.isEmpty()) return null;
        try {
            return Float.parseFloat(s.replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Nullable
    private static Integer parseIntSafe(String s) {
        if (s == null || s.isEmpty()) return null;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
