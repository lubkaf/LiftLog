package com.example.liftlog.ui.workout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.liftlog.R;
import com.example.liftlog.data.model.Exercise;
import com.example.liftlog.data.model.SessionSet;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class ActiveWorkoutFragment extends Fragment {

    private ActiveWorkoutViewModel viewModel;
    private WorkoutExerciseAdapter exerciseAdapter;

    private LinearLayout groupRestTimer;
    private TextView textRestRemaining;

    private List<Exercise> pickerExercises = new ArrayList<>();

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

        groupRestTimer = view.findViewById(R.id.group_rest_timer);
        textRestRemaining = view.findViewById(R.id.text_rest_remaining);
        RecyclerView recycler = view.findViewById(R.id.recycler_exercises);
        MaterialButton btnCancelTimer = view.findViewById(R.id.btn_cancel_timer);
        MaterialButton btnAddExercise = view.findViewById(R.id.btn_add_exercise);
        MaterialButton btnFinish = view.findViewById(R.id.btn_finish_workout);

        exerciseAdapter = new WorkoutExerciseAdapter(new WorkoutExerciseAdapter.WorkoutCallback() {
            @Override
            public void onLogSet(int exerciseId, float weightKg, int reps) {
                viewModel.logSet(exerciseId, weightKg, reps);
            }

            @Override
            public void onStartTimer(int seconds) {
                viewModel.startRestTimer(seconds);
            }

            @Override
            public void onDeleteSet(SessionSet set) {
                viewModel.deleteSet(set);
            }
        });

        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.setAdapter(exerciseAdapter);
        attachDragToRecycler(recycler);

        btnCancelTimer.setOnClickListener(v -> viewModel.cancelRestTimer());
        btnAddExercise.setOnClickListener(v -> showExercisePickerDialog());
        btnFinish.setOnClickListener(v -> showFinishDialog());

        viewModel.initialize(planId);

        viewModel.getExercises().observe(getViewLifecycleOwner(), exerciseAdapter::setExercises);
        viewModel.getAllSets().observe(getViewLifecycleOwner(), exerciseAdapter::updateSets);
        viewModel.getRestRemaining().observe(getViewLifecycleOwner(), this::renderRest);
        viewModel.getFinished().observe(getViewLifecycleOwner(), finished -> {
            if (Boolean.TRUE.equals(finished)) {
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_activeWorkout_to_summary);
            }
        });

        viewModel.getAllExercisesForPicker().observe(getViewLifecycleOwner(), exercises ->
                pickerExercises = exercises == null ? new ArrayList<>() : exercises);
    }

    private void attachDragToRecycler(RecyclerView recycler) {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {

            @Override
            public boolean onMove(@NonNull RecyclerView rv,
                                  @NonNull RecyclerView.ViewHolder from,
                                  @NonNull RecyclerView.ViewHolder to) {
                exerciseAdapter.moveItem(from.getAdapterPosition(), to.getAdapterPosition());
                return true;
            }

            @Override
            public void clearView(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh) {
                super.clearView(rv, vh);
                viewModel.setExercisesOrder(exerciseAdapter.getItems());
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int direction) {}

            @Override
            public boolean isLongPressDragEnabled() {
                return true;
            }
        }).attachToRecyclerView(recycler);
    }

    private void showExercisePickerDialog() {
        if (pickerExercises.isEmpty()) return;

        List<String> labels = new ArrayList<>(pickerExercises.size());
        for (Exercise e : pickerExercises) labels.add(e.name);

        Spinner spinner = new Spinner(requireContext());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, labels);
        spinner.setAdapter(adapter);

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.workout_select_exercise)
                .setView(spinner)
                .setPositiveButton(R.string.workout_add_exercise, (d, w) -> {
                    int pos = spinner.getSelectedItemPosition();
                    if (pos >= 0 && pos < pickerExercises.size()) {
                        viewModel.addPickedExercise(pickerExercises.get(pos).id);
                    }
                })
                .setNegativeButton(R.string.common_cancel, null)
                .show();
    }

    private void renderRest(int seconds) {
        if (seconds > 0) {
            groupRestTimer.setVisibility(View.VISIBLE);
            textRestRemaining.setText(getString(R.string.workout_rest_format, seconds));
        } else {
            groupRestTimer.setVisibility(View.GONE);
        }
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
}
