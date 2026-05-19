package com.example.liftlog.ui.library;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import com.example.liftlog.ui.planedit.PlanEditFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class LibraryFragment extends Fragment {

    private LibraryViewModel viewModel;
    private ExerciseAdapter adapter;
    private TextView textEmpty;
    private ArrayAdapter<String> groupSpinnerAdapter;
    private List<String> groupOptions = new ArrayList<>();
    private boolean pickerMode;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_library, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(LibraryViewModel.class);
        pickerMode = getArguments() != null && getArguments().getBoolean("pickerMode", false);

        textEmpty = view.findViewById(R.id.text_empty);
        Spinner spinner = view.findViewById(R.id.spinner_group);
        RecyclerView recycler = view.findViewById(R.id.recycler_exercises);
        ExtendedFloatingActionButton fab = view.findViewById(R.id.fab_add_custom);
        MaterialButton btnFetch = view.findViewById(R.id.btn_fetch_api);

        adapter = new ExerciseAdapter(this::onExerciseClicked);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.setAdapter(adapter);

        groupSpinnerAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, new ArrayList<>());
        spinner.setAdapter(groupSpinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                if (position < 0 || position >= groupOptions.size()) return;
                String selected = groupOptions.get(position);
                viewModel.selectGroup(position == 0 ? LibraryViewModel.GROUP_ALL : selected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        fab.setOnClickListener(v -> showAddDialog());
        btnFetch.setOnClickListener(v -> Toast.makeText(requireContext(),
                R.string.library_fetch_disabled_tooltip, Toast.LENGTH_SHORT).show());

        viewModel.getMuscleGroups().observe(getViewLifecycleOwner(), groups -> {
            groupOptions.clear();
            groupOptions.add(LibraryViewModel.GROUP_ALL);
            if (groups != null) groupOptions.addAll(groups);
            List<String> labels = new ArrayList<>(groupOptions.size());
            for (String g : groupOptions) {
                labels.add(LibraryViewModel.GROUP_ALL.equals(g)
                        ? getString(R.string.library_filter_all) : g);
            }
            groupSpinnerAdapter.clear();
            groupSpinnerAdapter.addAll(labels);
            groupSpinnerAdapter.notifyDataSetChanged();
        });
        viewModel.getFilteredExercises().observe(getViewLifecycleOwner(), list -> {
            adapter.submitList(list);
            textEmpty.setVisibility(list == null || list.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private void onExerciseClicked(Exercise exercise) {
        if (pickerMode) {
            Bundle result = new Bundle();
            result.putInt(PlanEditFragment.RESULT_BUNDLE_KEY, exercise.id);
            getParentFragmentManager().setFragmentResult(PlanEditFragment.RESULT_KEY, result);
            NavHostFragment.findNavController(this).popBackStack();
        }
        // W trybie zwykłym klik nie ma akcji (ekran szczegółów ćwiczenia w MVP pominięty).
    }

    private void showAddDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_exercise, null, false);
        TextInputEditText editName = dialogView.findViewById(R.id.edit_name);
        TextInputEditText editGroup = dialogView.findViewById(R.id.edit_group);
        TextInputEditText editDesc = dialogView.findViewById(R.id.edit_description);

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.add_exercise_title)
                .setView(dialogView)
                .setPositiveButton(R.string.common_save, (d, w) -> {
                    String name = textOf(editName);
                    String group = textOf(editGroup);
                    String desc = textOf(editDesc);
                    viewModel.addCustom(name, group, desc);
                })
                .setNegativeButton(R.string.common_cancel, null)
                .show();
    }

    private static String textOf(TextInputEditText edit) {
        return edit.getText() == null ? "" : edit.getText().toString();
    }
}
